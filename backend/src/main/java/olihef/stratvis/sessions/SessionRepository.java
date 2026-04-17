package olihef.stratvis.sessions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public SessionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public int createSession(long userId, Instant createdAt) {
        String sql = """
            INSERT INTO sessions(user_id, created_at)
            VALUES (?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, userId);
            statement.setString(2, createdAt.toString());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create session.");
        }
        return key.intValue();
    }

	public void deleteSession(int sessionId) {
		String deleteSnapshotsSql = "DELETE FROM session_snapshots WHERE session_id = ?";
		jdbcTemplate.update(deleteSnapshotsSql, sessionId);
		String deleteSessionSql = "DELETE FROM sessions WHERE id = ?";
		jdbcTemplate.update(deleteSessionSql, sessionId);
	}

    public int countSnapshots(int sessionId) {
        String sql = "SELECT COUNT(*) FROM session_snapshots WHERE session_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId);
        return count == null ? 0 : count;
    }

    public long addSnapshot(
        int sessionId,
        String label,
        double minLng,
        double minLat,
        double maxLng,
        double maxLat,
        int usedZoom,
        JsonNode analysis,
        Instant createdAt
    ) {
        String sql = """
            INSERT INTO session_snapshots(
                session_id, label, min_lng, min_lat, max_lng, max_lat, used_zoom, analysis_json, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, sessionId);
            statement.setString(2, label);
            statement.setDouble(3, minLng);
            statement.setDouble(4, minLat);
            statement.setDouble(5, maxLng);
            statement.setDouble(6, maxLat);
            statement.setInt(7, usedZoom);
            statement.setString(8, analysis == null ? null : analysis.toString());
            statement.setString(9, createdAt.toString());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create snapshot.");
        }
        return key.longValue();
    }

    public Session findOwnedSession(int sessionId, long userId) {
        String sessionSql = """
            SELECT id, created_at
            FROM sessions
            WHERE id = ? AND user_id = ?
            """;
        List<Session> sessions = jdbcTemplate.query(sessionSql, (rs, rowNum) -> new Session(
            rs.getInt("id"),
            Instant.parse(rs.getString("created_at")),
			null
        ), sessionId, userId);

        if (sessions.isEmpty()) {
            return null;
        }

        Session session = sessions.get(0);
		Snapshot snapshot = fetchSnapshotsBySessionId(sessionId).get(0);
		session.snapshot(snapshot);
        return session;
    }

    public boolean existsSession(int sessionId) {
        String sql = "SELECT COUNT(*) FROM sessions WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId);
        return count != null && count > 0;
    }

    public List<StoredSnapshot> findPendingOwnedSnapshots(int sessionId, long userId) {
        String sql = """
            SELECT ss.id, ss.label, ss.min_lng, ss.min_lat, ss.max_lng, ss.max_lat, ss.used_zoom, ss.created_at
            FROM session_snapshots ss
            INNER JOIN sessions s ON s.id = ss.session_id
            WHERE ss.session_id = ? AND s.user_id = ? AND ss.analysis_json IS NULL
            ORDER BY ss.id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StoredSnapshot(
            rs.getLong("id"),
            new Snapshot(
                rs.getString("label"),
                null,
                rs.getDouble("min_lng"),
                rs.getDouble("min_lat"),
                rs.getDouble("max_lng"),
                rs.getDouble("max_lat"),
                rs.getInt("used_zoom"),
                Instant.parse(rs.getString("created_at"))
            )
        ), sessionId, userId);
    }

    public void updateSnapshotAnalysis(long snapshotId, JsonNode analysis) {
        String sql = """
            UPDATE session_snapshots
            SET analysis_json = ?
            WHERE id = ?
            """;
        jdbcTemplate.update(sql, analysis.toString(), snapshotId);
    }

    public boolean isSessionOwnedByUser(int sessionId, long userId) {
        String sql = "SELECT COUNT(*) FROM sessions WHERE id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId, userId);
        return count != null && count > 0;
    }

    public List<Integer> findSessionIdsByUserId(long userId) {
        String sql = """
            SELECT id
            FROM sessions
            WHERE user_id = ?
            ORDER BY id DESC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id"), userId);
    }

    private List<Snapshot> fetchSnapshotsBySessionId(int sessionId) {
        String sql = """
            SELECT label, min_lng, min_lat, max_lng, max_lat, used_zoom, analysis_json, created_at
            FROM session_snapshots
            WHERE session_id = ?
            ORDER BY id ASC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String analysisJson = rs.getString("analysis_json");
            JsonNode analysis = parseAnalysis(analysisJson);
            return new Snapshot(
                rs.getString("label"),
                analysis,
                rs.getDouble("min_lng"),
                rs.getDouble("min_lat"),
                rs.getDouble("max_lng"),
                rs.getDouble("max_lat"),
                rs.getInt("used_zoom"),
                Instant.parse(rs.getString("created_at"))
            );
        }, sessionId);
    }

    private JsonNode parseAnalysis(String analysisJson) {
        if (analysisJson == null || analysisJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(analysisJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse stored analysis JSON.", e);
        }
    }

    public record StoredSnapshot(long id, Snapshot snapshot) {
    }
}