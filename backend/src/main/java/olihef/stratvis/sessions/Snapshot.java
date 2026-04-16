package olihef.stratvis.sessions;
import java.time.Instant;
import com.fasterxml.jackson.databind.JsonNode;

public class Snapshot {
	private String base64Image;
	private String label;
	private JsonNode analysis;
	private final Instant creationTime;

	public Snapshot(String base64Image, String label, JsonNode analysis) {
		this.base64Image = base64Image;
		this.label = label;
		this.analysis = analysis;
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
	public String base64Image() {
		return base64Image;
	}
	public Instant creationTime() {
		return creationTime;
	}
	public String toString() {
		return """
			{
				"label": "%s",
				"base64Image": "%s",
				"analysis": %s,
				"creationTime": "%s"
			}
			""".formatted(
			label,
			base64Image,
			analysis != null ? analysis.toString() : "null",
			creationTime.toString()
		);
	}
}