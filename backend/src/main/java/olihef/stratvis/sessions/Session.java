package olihef.stratvis.sessions;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import com.fasterxml.jackson.databind.JsonNode;

public class Session {
	private final int sessionId;
	private final Instant creationTime;
	private List<Snapshot> snapshots;

	public Session(int sessionId) {
		this.sessionId = sessionId;
		this.snapshots = new ArrayList<>();
		this.creationTime = Instant.now();
	}

	public Session addSnapshot(double minLng, double minLat, double maxLng, double maxLat, int usedZoom) {
		String label = "Image %d".formatted(snapshots.size());
		Snapshot snapshot = new Snapshot(label, null, minLng, minLat, maxLng, maxLat, usedZoom);
		this.snapshots.add(snapshot);
		return this;
	}

	public Session setAnalysis(JsonNode analysis, int index) {
		if (index < 0 || index >= this.snapshots.size()) {
			throw new IllegalArgumentException("Invalid image index for analysis.");
		}
		Snapshot snapshot = this.snapshots.get(index);
		snapshot.analysis(analysis);
		return this;
	}
	public int sessionId() {
		return sessionId;
	}
	public Instant creationTime() {
		return creationTime;
	}

	public String getLatestImageBase64() {
		throw new UnsupportedOperationException("Snapshots no longer store base64 images.");
	}

	public String toJson() {
		return """
			{
				"sessionId": %d,
				"snapshots": %s,
				"creationTime": "%s"
			}
			""".formatted(
			sessionId,
			snapshots.toString(),
			creationTime.toString()
		);
	}

	public List<Snapshot> snapshots() {
		return snapshots;
	}
}
