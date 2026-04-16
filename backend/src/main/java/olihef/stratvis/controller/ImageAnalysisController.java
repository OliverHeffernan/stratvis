package olihef.stratvis.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import olihef.stratvis.auth.AuthContext;
import olihef.stratvis.auth.AuthenticatedUser;
import olihef.stratvis.service.ImageAnalysisService;
import olihef.stratvis.service.TileStitchService;
import olihef.stratvis.sessions.Session;
import olihef.stratvis.sessions.SessionRepository;
import olihef.stratvis.sessions.SessionService;
import olihef.stratvis.sessions.Snapshot;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static olihef.stratvis.auth.AuthExceptions.UnauthorizedException;

@RestController
@RequestMapping("/api/v1")
public class ImageAnalysisController {

    private final ImageAnalysisService imageAnalysisService;
    private final TileStitchService tileStitchService;
    private final SessionService sessionService;

    public ImageAnalysisController(
        ImageAnalysisService imageAnalysisService,
        TileStitchService tileStitchService,
        SessionService sessionService
    ) {
        this.imageAnalysisService = imageAnalysisService;
        this.tileStitchService = tileStitchService;
        this.sessionService = sessionService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> analyzeImage(HttpServletRequest request, @RequestPart("image") MultipartFile image)
        throws IOException {
        AuthenticatedUser user = AuthContext.requireUser(request);
        JsonNode analysis = imageAnalysisService.analyze(image);
        sessionService.createSessionWithAnalysis(user.id(), analysis);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping(value = "/analyze-session", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> analyzeSessionImages(HttpServletRequest request, @RequestParam("sessionId") int sessionId)
        throws IOException {
        AuthenticatedUser user = AuthContext.requireUser(request);
        List<SessionRepository.StoredSnapshot> pendingSnapshots = sessionService.getPendingOwnedSnapshotsOrThrow(sessionId, user.id());

        for (SessionRepository.StoredSnapshot storedSnapshot : pendingSnapshots) {
            Snapshot snapshot = storedSnapshot.snapshot();
            byte[] stitchedPng = tileStitchService.stitchPng(
                snapshot.minLng(),
                snapshot.minLat(),
                snapshot.maxLng(),
                snapshot.maxLat(),
                snapshot.usedZoom()
            );
            String imageBase64 = Base64.getEncoder().encodeToString(stitchedPng);
            JsonNode analysis = imageAnalysisService.analyzeBase64Image(imageBase64, "image/png");
            JsonNode enrichedAnalysis = enrichPoiCoordinates(analysis, snapshot);
            sessionService.updateSnapshotAnalysis(storedSnapshot.id(), enrichedAnalysis);
        }

        Session session = sessionService.getOwnedSessionOrThrow(sessionId, user.id());
        return ResponseEntity.ok(sessionService.toJson(session));
    }

    @GetMapping(value = "/session", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSession(HttpServletRequest request, @RequestParam("sessionId") int sessionId) {
        AuthenticatedUser user = AuthContext.requireUser(request);
        Session session = sessionService.getOwnedSessionOrThrow(sessionId, user.id());
        return ResponseEntity.ok(sessionService.toJson(session));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
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

    private JsonNode enrichPoiCoordinates(JsonNode analysis, Snapshot snapshot) {
        if (!(analysis instanceof ObjectNode analysisObject)) {
            return analysis;
        }

        JsonNode poiNode = analysisObject.get("points_of_interest");
        if (!(poiNode instanceof ArrayNode poiArray)) {
            return analysis;
        }

        for (JsonNode poi : poiArray) {
            if (!(poi instanceof ObjectNode poiObject)) {
                continue;
            }

            JsonNode pointsNode = poiObject.get("points");
            if (!(pointsNode instanceof ArrayNode pointsArray) || pointsArray.isEmpty()) {
                continue;
            }

            double avgX = 0.0;
            double avgY = 0.0;
            int count = 0;
            for (JsonNode guess : pointsArray) {
                if (!guess.has("x") || !guess.has("y")) {
                    continue;
                }
                avgX += guess.path("x").asDouble();
                avgY += guess.path("y").asDouble();
                count++;
            }
            if (count == 0) {
                continue;
            }

            avgX = clamp01(avgX / count);
            avgY = clamp01(avgY / count);
            double lng = snapshot.minLng() + avgX * (snapshot.maxLng() - snapshot.minLng());
            double lat = mercatorInterpolateLat(snapshot.maxLat(), snapshot.minLat(), avgY);

            double rangePixels = poiObject.path("range").asDouble(0.0);
            double metersPerPixel = metersPerPixelAtLat(lat, snapshot.usedZoom());
            double rangeMeters = Math.max(0.0, rangePixels * metersPerPixel);

            poiObject.put("x", avgX);
            poiObject.put("y", avgY);
            poiObject.put("lng", lng);
            poiObject.put("lat", lat);
            poiObject.put("range_m", rangeMeters);
        }

        return analysisObject;
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double mercatorInterpolateLat(double topLat, double bottomLat, double t) {
        double topY = latToMercatorY(topLat);
        double bottomY = latToMercatorY(bottomLat);
        double y = topY + t * (bottomY - topY);
        return mercatorYToLat(y);
    }

    private double latToMercatorY(double lat) {
        double rad = Math.toRadians(lat);
        return Math.log(Math.tan(Math.PI / 4.0 + rad / 2.0));
    }

    private double mercatorYToLat(double y) {
        return Math.toDegrees(2.0 * Math.atan(Math.exp(y)) - Math.PI / 2.0);
    }

    private double metersPerPixelAtLat(double lat, int zoom) {
        if (zoom <= 0) {
            return 0.0;
        }
        return 156543.03392 * Math.cos(Math.toRadians(lat)) / Math.pow(2, zoom);
    }
}
