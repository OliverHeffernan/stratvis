package olihef.stratvis.geoapify;

import io.github.cdimascio.dotenv.Dotenv;
import olihef.stratvis.service.GeoapifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LabelMakingTest {
	private GeoapifyLabeller labeller;

	@BeforeEach
	void setUp() {
		String apiKey = resolveGeoapifyApiKey();
		Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "GEOAPIFY_API_KEY must be set for this integration test.");
		GeoapifyService geoapifyService = new GeoapifyService(apiKey);
		labeller = new GeoapifyLabeller(geoapifyService);
	}

	private String resolveGeoapifyApiKey() {
		String fromEnv = System.getenv("GEOAPIFY_API_KEY");
		if (fromEnv != null && !fromEnv.isBlank()) {
			return fromEnv;
		}

		Dotenv localDotenv = Dotenv.configure()
			.directory(".")
			.filename(".env")
			.ignoreIfMissing()
			.load();
		String fromLocalDotenv = localDotenv.get("GEOAPIFY_API_KEY");
		if (fromLocalDotenv != null && !fromLocalDotenv.isBlank()) {
			return fromLocalDotenv;
		}

		Dotenv backendDotenv = Dotenv.configure()
			.directory("backend")
			.filename(".env")
			.ignoreIfMissing()
			.load();
		return backendDotenv.get("GEOAPIFY_API_KEY");
	}

	@Test
	void oldTraffordTest() {
		double lng = -2.2913589860754655;
		double lat = 53.46303384998356;
		String label = this.labeller.label(lng, lat);
		assertTrue(!label.equals("Placeholder"), "Geoapify label should not fall back to Placeholder.");
		assertTrue(label.contains("United Kingdom"), "Expected UK location label, got: " + label);
	}
}
