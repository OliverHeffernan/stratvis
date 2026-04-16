package olihef.stratvis;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StratvisBackendApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String dotenvApiKey = dotenv.get("OPENAI_API_KEY");
        String sqliteDbPath = dotenv.get("SQLITE_DB_PATH");
        if (dotenvApiKey != null && !dotenvApiKey.isBlank() && System.getenv("OPENAI_API_KEY") == null) {
            System.setProperty("OPENAI_API_KEY", dotenvApiKey);
        }
        if (sqliteDbPath != null && !sqliteDbPath.isBlank() && System.getenv("SQLITE_DB_PATH") == null) {
            System.setProperty("SQLITE_DB_PATH", sqliteDbPath);
        }
        SpringApplication.run(StratvisBackendApplication.class, args);
    }
}
