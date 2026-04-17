package olihef.stratvis.sessions;

import java.time.Instant;

public class Session {
	private final int sessionId;
	private final Instant creationTime;
	private Snapshot snapshot;

	public Session(int sessionId, Instant creationTime, Snapshot snapshot) {
		this.sessionId = sessionId;
		this.snapshot = snapshot;
		this.creationTime = creationTime;
	}

	public int sessionId() {
		return sessionId;
	}

	public Instant creationTime() {
		return creationTime;
	}

	public Snapshot snapshot() {
		return snapshot;
	}

	public void snapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}
}