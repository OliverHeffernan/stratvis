package olihef.stratvis.sessions;

import java.time.Instant;
import com.fasterxml.jackson.databind.JsonNode;

public class Snapshot {
	private String label;
	private final JsonNode analysis;
	private final Instant creationTime;
	private final double minLng;
	private final double minLat;
	private final double maxLng;
	private final double maxLat;
	private final int usedZoom;

	public Snapshot(String label, JsonNode analysis, double minLng, double minLat, double maxLng, double maxLat, int usedZoom) {
		this(label, analysis, minLng, minLat, maxLng, maxLat, usedZoom, Instant.now());
	}

	public Snapshot(String label, JsonNode analysis, double minLng, double minLat, double maxLng, double maxLat, int usedZoom, Instant creationTime) {
		this.label = label;
		this.analysis = analysis;
		this.minLng = minLng;
		this.minLat = minLat;
		this.maxLng = maxLng;
		this.maxLat = maxLat;
		this.usedZoom = usedZoom;
		this.creationTime = creationTime;
	}

	public void label(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public JsonNode analysis() {
		return analysis;
	}

	public double minLng() {
		return minLng;
	}

	public double minLat() {
		return minLat;
	}

	public double maxLng() {
		return maxLng;
	}

	public double maxLat() {
		return maxLat;
	}

	public int usedZoom() {
		return usedZoom;
	}

	public Instant creationTime() {
		return creationTime;
	}
}
