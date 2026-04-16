package olihef.stratvis.controller;

import jakarta.servlet.http.HttpServletRequest;
import olihef.stratvis.auth.AuthContext;
import olihef.stratvis.auth.AuthService;
import olihef.stratvis.auth.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static olihef.stratvis.auth.AuthExceptions.UnauthorizedException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest payload) {
        AuthService.AuthResponse response = authService.register(payload.email(), payload.displayName(), payload.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseBody(response));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest payload) {
        AuthService.AuthResponse response = authService.login(payload.email(), payload.password());
        return ResponseEntity.ok(toResponseBody(response));
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logoutByRawToken(authHeader.substring("Bearer ".length()).trim());
        }
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        AuthenticatedUser user = AuthContext.requireUser(request);
        return ResponseEntity.ok(Map.of(
            "userId", user.id(),
            "email", user.email(),
            "displayName", user.displayName()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    private Map<String, Object> toResponseBody(AuthService.AuthResponse response) {
        return Map.of(
            "user", Map.of(
                "id", response.userId(),
                "email", response.email(),
                "displayName", response.displayName()
            ),
            "token", response.token(),
            "expiresAt", response.expiresAt()
        );
    }

    public record RegisterRequest(String email, String displayName, String password) {
    }

    public record LoginRequest(String email, String password) {
    }
}
