package com.example.vladyslav.service;

import com.example.vladyslav.awsS3.AwsS3Service;
import com.example.vladyslav.dto.UserDTO;
import com.example.vladyslav.model.Patient;
import com.example.vladyslav.model.User;
import com.example.vladyslav.model.enums.Role;
import com.example.vladyslav.repository.PatientRepository;
import com.example.vladyslav.repository.UserRepository;
import com.example.vladyslav.requests.PatientRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private  final PasswordEncoder passwordEncoder;
    private  final JwtEncoder jwtEncoder;
    private  final AwsS3Service awsS3Service;

    @org.springframework.beans.factory.annotation.Value("${app.jwt.issuer:medikart-api}")
    private String issuer;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public String login(String email, String rawPassword) {

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isEmpty() || rawPassword == null || rawPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User u = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (u.getPassword() == null || u.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        boolean ok = passwordEncoder.matches(rawPassword, u.getPassword());

        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600)) // 1h
                .subject(String.valueOf(u.getId()))
                .claim("email", u.getEmail())
                .claim("role", u.getRole()) // single role
                .build();

        var headers = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    @Transactional
    public UserDTO register(PatientRegisterRequest r) {
        String normalizedEmail = r.getEmail() == null ? "" : r.getEmail().trim().toLowerCase();
        if (normalizedEmail.isEmpty() || r.getPassword() == null || r.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        if (userRepository.existsByEmail(normalizedEmail))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");

        String imageUrl = awsS3Service.saveImageToS3(r.getImage());

        // 1) create user
        User user = User.builder()
                .email(r.getEmail())
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.PATIENT)
                .build();
        User savedUser = userRepository.save(user);

        // 2) create patient
        Patient p = Patient.builder()
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .phoneNumber(r.getPhoneNumber())
                .email(r.getEmail())
                .dateOfBirth(r.getDateOfBirth())
                .imageUrl(imageUrl)
                .user(user)                 // IMPORTANT
                .build();
        patientRepository.save(p);


        // 3) return dto
        return UserDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }
}


//    public AuthService(
//            UserRepository userRepository,
//            PatientRepository patientRepository,
//            PasswordEncoder passwordEncoder,
//            JwtEncoder jwtEncoder,
//            @org.springframework.beans.factory.annotation.Value("${app.jwt.issuer:medikart-api}") String issuer
//    ) {
//        this.userRepository = userRepository;
//        this.patientRepository = patientRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtEncoder = jwtEncoder;
//        this.issuer = issuer;
//    }
