package olihef.stratvis.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("PRAGMA foreign_keys = ON");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE,
                display_name TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
            """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS auth_tokens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                token_hash TEXT NOT NULL UNIQUE,
                created_at TEXT NOT NULL,
                expires_at TEXT NOT NULL,
                revoked_at TEXT,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
            """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
            """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS session_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                label TEXT NOT NULL,
                min_lng REAL NOT NULL,
                min_lat REAL NOT NULL,
                max_lng REAL NOT NULL,
                max_lat REAL NOT NULL,
                used_zoom INTEGER NOT NULL,
                analysis_json TEXT,
                created_at TEXT NOT NULL,
                FOREIGN KEY(session_id) REFERENCES sessions(id)
            )
            """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_auth_tokens_hash ON auth_tokens(token_hash)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_session_snapshots_session_id ON session_snapshots(session_id)");
    }
}
