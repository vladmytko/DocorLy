package com.example.vladyslav.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;

/**
 * Configuration class for validating JWT tokens issued by Google.
 * It sets up a JwtDecoder bean that validates the issuer and audience of the token.
 */

@Configuration
public class JWTValidationConfig {
    // The expected issuer of the JWT tokens (Google accounts)
    private static final String ISSUER = "https://accounts.google.com";

    @org.springframework.beans.factory.annotation.Value("${google.client.id}")
    private String googleClientId;

    @Bean
    JwtDecoder jwtDecoder() {
        // Create a NimbusJwtDecoder based on the issuer's location (Google's OAuth2 endpoint)
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(ISSUER);

        // Validator to check that the token's issuer matches the expected issuer
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(ISSUER);

        OAuth2TokenValidator<Jwt> withAudience = jwt -> {
            java.util.List<String> aud = jwt.getAudience();
            boolean ok = aud != null && aud.contains(googleClientId);
            return ok
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
        };

        // Combine both issuer and audience validators
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

}
