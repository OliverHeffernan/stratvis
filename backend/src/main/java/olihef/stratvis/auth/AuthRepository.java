package olihef.stratvis.auth;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuthRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserRecord findUserByEmail(String email) {
        String sql = """
            SELECT id, email, display_name, password_hash, created_at
            FROM users
            WHERE email = ?
            """;
        List<UserRecord> users = jdbcTemplate.query(sql, (rs, rowNum) -> new UserRecord(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("display_name"),
            rs.getString("password_hash"),
            Instant.parse(rs.getString("created_at"))
        ), email);
        return users.isEmpty() ? null : users.get(0);
    }

    public UserRecord findUserById(long id) {
        String sql = """
            SELECT id, email, display_name, password_hash, created_at
            FROM users
            WHERE id = ?
            """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new UserRecord(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("display_name"),
                rs.getString("password_hash"),
                Instant.parse(rs.getString("created_at"))
            ), id);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public long insertUser(String email, String displayName, String passwordHash, Instant createdAt) {
        String sql = """
            INSERT INTO users(email, display_name, password_hash, created_at)
            VALUES (?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, email);
            statement.setString(2, displayName);
            statement.setString(3, passwordHash);
            statement.setString(4, createdAt.toString());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create user.");
        }
        return key.longValue();
    }

    public void insertToken(long userId, String tokenHash, Instant createdAt, Instant expiresAt) {
        String sql = """
            INSERT INTO auth_tokens(user_id, token_hash, created_at, expires_at)
            VALUES (?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
            userId,
            tokenHash,
            createdAt.toString(),
            expiresAt.toString()
        );
    }

    public AuthenticatedUser findAuthenticatedUserByTokenHash(String tokenHash, Instant now) {
        String sql = """
            SELECT u.id, u.email, u.display_name
            FROM auth_tokens t
            INNER JOIN users u ON u.id = t.user_id
            WHERE t.token_hash = ?
              AND t.revoked_at IS NULL
              AND t.expires_at > ?
            """;
        List<AuthenticatedUser> users = jdbcTemplate.query(sql, (rs, rowNum) -> new AuthenticatedUser(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("display_name")
        ), tokenHash, now.toString());
        return users.isEmpty() ? null : users.get(0);
    }

    public void revokeTokenByHash(String tokenHash, Instant revokedAt) {
        String sql = """
            UPDATE auth_tokens
            SET revoked_at = ?
            WHERE token_hash = ?
              AND revoked_at IS NULL
            """;
        jdbcTemplate.update(sql, revokedAt.toString(), tokenHash);
    }

    public record UserRecord(long id, String email, String displayName, String passwordHash, Instant createdAt) {
    }
}
