package com.example.vladyslav.controller;

import com.example.vladyslav.dto.AuthResponse;
import com.example.vladyslav.dto.UserDTO;
import com.example.vladyslav.requests.LoginRequest;
import com.example.vladyslav.requests.PatientRegisterRequest;
import com.example.vladyslav.service.AuthService;
import com.example.vladyslav.service.UserService;
import com.example.vladyslav.service.JWTService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    @Value("${google.client.id}")
    private String clientId;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthService auth;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        String token = auth.login(req.getEmail(), req.getPassword());
        UserDTO user = userService.getUserByEmail(req.getEmail());

        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setUser(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(user)
                .build());
    }

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDTO> register(@Valid @ModelAttribute PatientRegisterRequest req) {
        UserDTO user = auth.register(req);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.badRequest().build();
    }


//    @PostMapping("/google")
//    public ResponseEntity<?> authenticate(@RequestBody Map<String, Object> request) {
//        try{
//            JsonFactory jsonFactory = new GsonFactory();
//            String idToken = (String) request.get("idToken");
//
//            if (clientId == null || clientId.isBlank()) {
//                return ResponseEntity.internalServerError().body("google.client.id is not configured on the server");
//            }
//
//
//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
//                    .setAudience(Collections.singletonList(clientId)).build();
//
//            GoogleIdToken idToken1 = verifier.verify(idToken);
//            if(idToken1 != null){
//                GoogleIdToken.Payload payload = idToken1.getPayload();
//
//                if(!payload.getAudience().equals(clientId)){
//                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid audience");
//                }
//                String email = payload.getEmail();
//                String name = payload.get("name") != null ? payload.get("name").toString() :payload.getEmail();
//                User user = userDetailsService.getUserByEmail(email);
//                if (user != null && email.equals(user.getEmail())) {
//                    // Existing user -> issue app JWT
//                    String token = jwtService.issueAccessToken(user);
//                    return ResponseEntity.ok().body(Map.of(
//                            "token", token,
//                            "email", email
//                    ));
//                } else {
//                    // Register new user then issue app JWT
//                    User created = userDetailsService.createUser(request, payload);
//                    String token = jwtService.issueAccessToken(created);
//                    return ResponseEntity.ok().body(Map.of(
//                            "token", token,
//                            "email", email
//                    ));
//                }
//            }
//            return ResponseEntity.badRequest().body("Invalid Request");
//
//        }
//        catch (Exception e) {
//            System.out.println("Error "+e);
//            return ResponseEntity.internalServerError().body("Something went wrong");
//        }
//    }

//    @PostMapping("/google")
//    public ResponseEntity<?> authenticate2(@RequestBody Map<String,Object> request){
//        try{
//            JsonFactory jsonFactory = new GsonFactory();
//            String idToken = (String) request.get("idToken");
//
//            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
//                    .setAudience(Collections.singletonList(clientId)).build();
//
//            GoogleIdToken idToken1 = verifier.verify(idToken);
//            if(idToken1 != null){
//                GoogleIdToken.Payload payload = idToken1.getPayload();
//
//                if(!payload.getAudience().equals(clientId)){
//
//                }
//                String email = payload.getEmail();
//                String name = payload.getEmail();
//                User user = userDetailsService.getUserByEmail(email);
//                if(null != user && user.getEmail().equals(email)){
//                    // Create JWT token and return it
//                    String token = jwtTokenHelper.generateToken(user);
//                    AuthResponse authResponse = AuthResponse.builder()
//                            .token(token).build();
//                    return ResponseEntity.ok().body(authResponse);
//                }
//                else {
//                    // Register new user
//                    User userOnj = userDetailsService.createUser(request, payload);
//
//                    String token = jwtTokenHelper.generateToken(user);
//                    AuthResponse authResponse = AuthResponse.builder()
//                            .token(token).build();
//                    return ResponseEntity.ok().body(authResponse);
//
//                }
//            }
//            return ResponseEntity.badRequest().body("Invalid Request");
//
//        }
//        catch (Exception e) {
//            System.out.println("Error "+e);
//            return ResponseEntity.internalServerError().body("Something went wrong");
//        }
//    }
}
