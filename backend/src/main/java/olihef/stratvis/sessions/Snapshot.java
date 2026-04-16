package olihef.stratvis.sessions;

import java.time.Instant;
import com.fasterxml.jackson.databind.JsonNode;

public class Snapshot {
	private String label;
	private JsonNode analysis;
	private final Instant creationTime;
	private final double minLng;
	private final double minLat;
	private final double maxLng;
	private final double maxLat;
	private final int usedZoom;

	public Snapshot(String label, JsonNode analysis, double minLng, double minLat, double maxLng, double maxLat, int usedZoom) {
		this.label = label;
		this.analysis = analysis;
		this.minLng = minLng;
		this.minLat = minLat;
		this.maxLng = maxLng;
		this.maxLat = maxLat;
		this.usedZoom = usedZoom;
		this.creationTime = Instant.now();
	}
	public void label(String label) {
		this.label = label;
	}
	public String label() {
		return label;
	}
	public void analysis(JsonNode analysis) {
		this.analysis = analysis;
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
	public String toString() {
		return """
			{
				"label": "%s",
				"minLng": %s,
				"minLat": %s,
				"maxLng": %s,
				"maxLat": %s,
				"usedZoom": %d,
				"analysis": %s,
				"creationTime": "%s"
			}
			""".formatted(
			label,
			minLng,
			minLat,
			maxLng,
			maxLat,
			usedZoom,
			analysis != null ? analysis.toString() : "null",
			creationTime.toString()
		);
	}
}
