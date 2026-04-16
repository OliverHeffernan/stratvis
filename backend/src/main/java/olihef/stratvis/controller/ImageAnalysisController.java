package olihef.stratvis.controller;

import olihef.stratvis.service.ImageAnalysisService;
import olihef.stratvis.sessions.*;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ImageAnalysisController {

    private final ImageAnalysisService imageAnalysisService;

    public ImageAnalysisController(ImageAnalysisService imageAnalysisService) {
        this.imageAnalysisService = imageAnalysisService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> analyzeImage(@RequestPart("image") MultipartFile image) throws IOException {
		JsonNode analysis = imageAnalysisService.analyze(image);
		SessionsManager.addNewSessionWithImageAndAnalysis(image, analysis);
        return ResponseEntity.ok(analysis);
    }

	// also allow user to send a session id instead of an image, in which case we will analyze the image from that session
	@PostMapping(value = "/analyze-session", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> analyzeSessionImages(@RequestParam("sessionId") int sessionId) throws IOException, IllegalStateException {
		Session session = SessionsManager.getSession(sessionId);
		for (Snapshot snapshot : session.snapshots()) {
			if (snapshot.analysis() != null) {
				continue;
			}
			String imageBase64 = snapshot.base64Image();
			JsonNode analysis = imageAnalysisService.analyzeBase64Image(imageBase64, "image/png");
			snapshot.analysis(analysis);
		}
		return ResponseEntity.ok(session.toJson());
	}

	public ResponseEntity<JsonNode> analyzeSessionImage(@RequestParam("sessionId") int sessionId) throws IOException, IllegalStateException {
		Session session = SessionsManager.getSession(sessionId);
		String imageBase64 = session.getLatestImageBase64();
		JsonNode analysis = imageAnalysisService.analyzeBase64Image(imageBase64, "image/png");
		return ResponseEntity.ok(analysis);
	}

	@PostMapping(value = "/upload-and-analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> uploadAndAnalyzeImage(@RequestPart("image") MultipartFile image, @RequestPart("sessionId") int sessionId) throws IOException {
		Session session = SessionsManager.getSession(sessionId);
		String base64Image = java.util.Base64.getEncoder().encodeToString(image.getBytes());
		session.addImage(base64Image);
		JsonNode analysis = imageAnalysisService.analyzeBase64Image(base64Image, image.getContentType());
		session.setAnalysis(analysis, session.snapshots().size() - 1);
		return ResponseEntity.ok(session.toJson());
	}

	@GetMapping(value = "/session", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getSession(@RequestParam("sessionId") int sessionId) throws IllegalStateException {
		Session session = SessionsManager.getSession(sessionId);
		return ResponseEntity.ok(session.toJson());
	}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIoException(IOException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "Failed to fetch or process imagery.",
                "details", ex.getMessage() == null ? "No additional details available." : ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConfigOrParsingError(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Map<String, Object>> handleProviderHttpError(HttpStatusCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "OpenAI API returned an error.",
                "provider_status", ex.getStatusCode().value(),
                "provider_body", ex.getResponseBodyAsString()
        ));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleProviderOrIoError(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "Failed to analyze image with OpenAI API.",
                "details", ex.getMessage() == null ? "No additional details available." : ex.getMessage()
        ));
    }
}