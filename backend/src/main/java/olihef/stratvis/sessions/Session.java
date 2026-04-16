package olihef.stratvis.sessions;

import java.util.List;
import java.time.Instant;

public class Session {
	private final int sessionId;
	private final Instant creationTime;
	private final List<Snapshot> snapshots;

	public Session(int sessionId, Instant creationTime, List<Snapshot> snapshots) {
		this.sessionId = sessionId;
		this.snapshots = snapshots;
		this.creationTime = creationTime;
	}

	public int sessionId() {
		return sessionId;
	}

	public Instant creationTime() {
		return creationTime;
	}

	public List<Snapshot> snapshots() {
		return snapshots;
	}
}
