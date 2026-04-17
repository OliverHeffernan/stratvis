package olihef.stratvis.sessions;
import java.time.Instant;
import java.util.Map;
public record SessionInfo(
	int sessionId,
	Instant creationTime,
	String label
) {
	public static SessionInfo of(Session session) {
		return new SessionInfo(session.sessionId(), session.creationTime(), session.snapshot().label());
	}

	public Map<String, String> toMap() {
		return Map.of(
			"sessionId", String.valueOf(sessionId),
			"creationTime", creationTime.toString(),
			"label", label
		);
	}
}