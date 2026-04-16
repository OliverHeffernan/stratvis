package olihef.stratvis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;

@Service
public class FeedbackService {

	private final Path feedbackLogPath;
	private final ObjectMapper objectMapper;
	private final Object writeLock = new Object();

	public FeedbackService(
		ObjectMapper objectMapper,
		@Value("${feedback.log.file:/var/log/stratvis/feedback.log}") String feedbackLogFile
	) {
		this.objectMapper = objectMapper;
		this.feedbackLogPath = Path.of(feedbackLogFile);
	}

	public String appendFeedback(String name, String message) throws IOException {
		String cleanName = sanitize(name, "name");
		String cleanMessage = sanitize(message, "message");
		String timestamp = Instant.now().toString();

		String line = toJsonLine(cleanName, cleanMessage, timestamp);

		synchronized (writeLock) {
			Path parent = feedbackLogPath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(
				feedbackLogPath,
				line + System.lineSeparator(),
				StandardOpenOption.CREATE,
				StandardOpenOption.APPEND
			);
		}

		return timestamp;
	}

	private String sanitize(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		return value.trim();
	}

	private String toJsonLine(String name, String message, String timestamp) {
		try {
			return objectMapper.writeValueAsString(Map.of(
				"name", name,
				"message", message,
				"timestamp", timestamp
			));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize feedback entry.", e);
		}
	}
}
