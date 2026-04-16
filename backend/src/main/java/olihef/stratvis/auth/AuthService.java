package olihef.stratvis.auth;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import static olihef.stratvis.auth.AuthExceptions.UnauthorizedException;

@Service
public class AuthService {

    private static final int BCRYPT_COST = 12;

    private final AuthRepository authRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long tokenTtlSeconds;

    public AuthService(AuthRepository authRepository, @Value("${auth.token.ttl.seconds:2592000}") long tokenTtlSeconds) {
        this.authRepository = authRepository;
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    public AuthResponse register(String email, String displayName, String password) {
        String cleanEmail = sanitizeEmail(email);
        String cleanDisplayName = sanitizeDisplayName(displayName);
        String cleanPassword = sanitizePassword(password);

        Instant now = Instant.now();
        String passwordHash = BCrypt.hashpw(cleanPassword, BCrypt.gensalt(BCRYPT_COST));

        long userId;
        try {
            userId = authRepository.insertUser(cleanEmail, cleanDisplayName, passwordHash, now);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        TokenIssue issuedToken = issueToken(userId, now);
        return new AuthResponse(userId, cleanEmail, cleanDisplayName, issuedToken.token(), issuedToken.expiresAt().toString());
    }

    public AuthResponse login(String email, String password) {
        String cleanEmail = sanitizeEmail(email);
        String cleanPassword = sanitizePassword(password);

        AuthRepository.UserRecord user = authRepository.findUserByEmail(cleanEmail);
        if (user == null || !BCrypt.checkpw(cleanPassword, user.passwordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        Instant now = Instant.now();
        TokenIssue issuedToken = issueToken(user.id(), now);
        return new AuthResponse(user.id(), user.email(), user.displayName(), issuedToken.token(), issuedToken.expiresAt().toString());
    }

    public void logoutByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        authRepository.revokeTokenByHash(hashToken(rawToken), Instant.now());
    }

    public AuthenticatedUser authenticateRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        return authRepository.findAuthenticatedUserByTokenHash(hashToken(rawToken), Instant.now());
    }

    public AuthenticatedUser getUserById(long userId) {
        AuthRepository.UserRecord user = authRepository.findUserById(userId);
        if (user == null) {
            return null;
        }
        return new AuthenticatedUser(user.id(), user.email(), user.displayName());
    }

    private TokenIssue issueToken(long userId, Instant now) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = now.plusSeconds(tokenTtlSeconds);
        authRepository.insertToken(userId, tokenHash, now, expiresAt);
        return new TokenIssue(rawToken, expiresAt);
    }

    private String sanitizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return email.trim().toLowerCase();
    }

    private String sanitizeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name is required.");
        }
        String value = displayName.trim();
        if (value.length() > 120) {
            throw new IllegalArgumentException("Display name is too long.");
        }
        return value;
    }

    private String sanitizePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        String value = password.trim();
        if (value.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        return value;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    public record AuthResponse(long userId, String email, String displayName, String token, String expiresAt) {
    }

    private record TokenIssue(String token, Instant expiresAt) {
    }
}
