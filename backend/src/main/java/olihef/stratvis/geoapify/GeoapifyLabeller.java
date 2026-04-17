package olihef.stratvis.geoapify;
import olihef.stratvis.service.GeoapifyService;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;

@Component
public class GeoapifyLabeller {
	private final GeoapifyService service;
	public GeoapifyLabeller(GeoapifyService service) {
		this.service = service;
	}
	public String label(double lng, double lat) {
		String body = this.service.fetch(lng, lat);
		try {
			JsonNode json = stringToJsonNode(body);
			JsonNode firstResult = resolveFirstResult(json);
			if (firstResult == null) {
				return "Placeholder";
			}

			String city = readFirstNonBlank(firstResult, "city", "town", "village", "county", "state");
			String country = readFirstNonBlank(firstResult, "country");
			if (city == null || country == null) {
				return "Placeholder";
			}
			return city + ", " + country;
		} catch (IOException e) {
			return "Placeholder";
		}
	}

	private JsonNode resolveFirstResult(JsonNode json) {
		JsonNode results = json.get("results");
		if (results != null && results.isArray() && !results.isEmpty()) {
			return results.get(0);
		}
		JsonNode features = json.get("features");
		if (features != null && features.isArray() && !features.isEmpty()) {
			JsonNode properties = features.get(0).get("properties");
			if (properties != null && properties.isObject()) {
				return properties;
			}
		}
		return null;
	}

	private String readFirstNonBlank(JsonNode source, String... keys) {
		for (String key : keys) {
			JsonNode node = source.get(key);
			if (node == null || node.isNull()) {
				continue;
			}
			String value = node.asText();
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}
	private static JsonNode stringToJsonNode(String json) throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(json);
	}
}
