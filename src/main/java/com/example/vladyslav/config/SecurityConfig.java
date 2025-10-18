package com.example.vladyslav.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMongoAuditing(modifyOnCreate = true)
public class SecurityConfig {

    @Value("${google.client.id}")
    String googleClientId;

    @Value("${app.jwt.secret}")          // Base64-encoded HS256 key
    String appSecretB64;

    @Value("${app.jwt.issuer}")          // e.g. "medikart-api"
    String appIssuer;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    AuthenticationManagerResolver<HttpServletRequest> amr) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(o -> o.authenticationManagerResolver(amr));
        return http.build();
    }

    // --------- Decoders ---------

    @Bean
    JwtDecoder googleJwtDecoder() {
        // Uses Google’s OIDC issuer; adds audience check for your client id
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation("https://accounts.google.com");
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://accounts.google.com");
        OAuth2TokenValidator<Jwt> withAudience = token ->
                token.getAudience() != null && token.getAudience().contains(googleClientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Invalid audience",""));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    JwtDecoder appJwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(appSecretB64); // your secret is Base64 in properties
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(appIssuer);
        decoder.setJwtValidator(withIssuer); // (optional) add your own audience check here if you use one
        return decoder;
    }

    // Reuse one converter for both token types
    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
            // roles → ROLE_*
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null && !roles.isEmpty()) {
                List<GrantedAuthority> authorities = roles.stream()
                        .filter(Objects::nonNull)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .map(a -> (GrantedAuthority) a)
                        .collect(Collectors.toList());
                return authorities;
            }

            // Google scopes → SCOPE_*
            List<String> scopes = Optional.ofNullable(jwt.getClaimAsStringList("scope"))
                    .orElse(jwt.getClaimAsStringList("scp"));
            if (scopes != null && !scopes.isEmpty()) {
                List<GrantedAuthority> authorities = scopes.stream()
                        .map(s -> "SCOPE_" + s)
                        .map(SimpleGrantedAuthority::new)
                        .map(a -> (GrantedAuthority) a)
                        .collect(Collectors.toList());
                return authorities;
            }

            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        });
        return conv;
    }

    // --------- AuthenticationManagerResolver (issuer-based) ---------

    @Bean
    AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
            JwtDecoder googleJwtDecoder,
            JwtDecoder appJwtDecoder,
            Converter<Jwt, ? extends AbstractAuthenticationToken> converter) {

        // Provider for Google
        var googleProvider = new JwtAuthenticationProvider(googleJwtDecoder);
        googleProvider.setJwtAuthenticationConverter(converter);

        // Provider for your HS256 tokens
        var appProvider = new JwtAuthenticationProvider(appJwtDecoder);
        appProvider.setJwtAuthenticationConverter(converter);

        var byIssuer = new HashMap<String, AuthenticationManager>();
        byIssuer.put("https://accounts.google.com", new ProviderManager(googleProvider));
        byIssuer.put("medikart-api", new ProviderManager(appProvider));

        // Spring will peek at the unsigned 'iss' claim to choose the right manager
        return new JwtIssuerAuthenticationManagerResolver(issuer -> byIssuer.get(issuer));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // default strength 10
    }

    @Bean
    public JwtEncoder appJwtEncoder(@Value("${app.jwt.secret}") String appSecretB64) {
        byte[] keyBytes = Base64.getDecoder().decode(appSecretB64);
        var jwk = new OctetSequenceKey.Builder(keyBytes).algorithm(JWSAlgorithm.HS256).build();
        var jwkSource = (com.nimbusds.jose.jwk.source.JWKSource<SecurityContext>) (selector, ctx) ->
                java.util.List.of(jwk);
        return new NimbusJwtEncoder(jwkSource);
    }

}


//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .logout(AbstractHttpConfigurer::disable)
//                .exceptionHandling(custom ->
//                        custom.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
//                            .authorizeHttpRequests((authorize) -> authorize
//                                    .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
//                                    .requestMatchers("/api/auth/**").permitAll()
//                                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                                    .anyRequest().authenticated()).sessionManagement(httpSession -> httpSession
//                                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                                        .oauth2ResourceServer(oauth -> oauth
//                                                .jwt(jwt -> jwt
//                                                        .decoder(appJwtDecoder())
//                                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
//                                                )
//                                        );
//        return http.build();
//    }
//
//    @Bean
//    public JwtDecoder appJwtDecoder() {
//        var key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
//    }
//
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        var conv = new JwtAuthenticationConverter();
//        conv.setJwtGrantedAuthoritiesConverter(jwt -> {
//            var roles = jwt.getClaimAsStringList("roles");
//            if (roles == null) {
//                return Collections.<GrantedAuthority>emptyList();
//            }
//            return roles.stream()
//                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
//                    .collect(Collectors.toList());
//        });
//        return conv;
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        var config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:3000"));
//        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//
//        var source = new UrlBasedCorsConfigurationSource( );
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }

//    @Autowired
//    private JWTTokenHelper jwtTokenHelper;
//
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    private JWTAuthenticationFilter jwtAuthenticationFilter;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests((authorize)-> authorize
//                    .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .anyRequest().authenticated())
//                .sessionManagement(httpSession -> httpSession.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }

