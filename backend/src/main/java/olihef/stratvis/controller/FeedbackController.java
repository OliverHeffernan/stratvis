package olihef.stratvis.controller;

import jakarta.servlet.http.HttpServletRequest;
import olihef.stratvis.auth.AuthContext;
import olihef.stratvis.auth.AuthenticatedUser;
import olihef.stratvis.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static olihef.stratvis.auth.AuthExceptions.UnauthorizedException;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {

	private final FeedbackService feedbackService;

	public FeedbackController(FeedbackService feedbackService) {
		this.feedbackService = feedbackService;
	}

	@PostMapping(value = "/feedback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> submitFeedback(HttpServletRequest request, @RequestBody FeedbackRequest payload) throws IOException {
		AuthenticatedUser user = AuthContext.requireUser(request);
		String timestamp = feedbackService.appendFeedback(user.displayName(), payload.message());
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
			"status", "saved",
			"timestamp", timestamp
		));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleBadInput(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<Map<String, String>> handleIoError(IOException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
			"error", "Failed to persist feedback.",
			"details", ex.getMessage() == null ? "No additional details available." : ex.getMessage()
		));
	}

	public record FeedbackRequest(String message) {
	}
}
