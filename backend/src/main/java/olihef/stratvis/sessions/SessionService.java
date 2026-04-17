package olihef.stratvis.sessions;

import olihef.stratvis.geoapify.GeoapifyLabeller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper;
	private final GeoapifyLabeller geoapifyLabeller;

    public SessionService(SessionRepository sessionRepository, ObjectMapper objectMapper, GeoapifyLabeller geoapifyLabeller) {
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
		this.geoapifyLabeller = geoapifyLabeller;
    }

    public int createSessionWithSnapshot(
        long userId,
        double minLng,
        double minLat,
        double maxLng,
        double maxLat,
        int usedZoom
    ) {
        Instant now = Instant.now();
        int sessionId = sessionRepository.createSession(userId, now);
		double lng = (minLng + maxLng) / 2.0;
		double lat = (minLat + maxLat) / 2.0;
		String label = this.geoapifyLabeller.label(lng, lat);
        sessionRepository.addSnapshot(sessionId, label, minLng, minLat, maxLng, maxLat, usedZoom, null, now);
        return sessionId;
    }

    public int createSessionWithAnalysis(long userId, com.fasterxml.jackson.databind.JsonNode analysis) {
        Instant now = Instant.now();
        int sessionId = sessionRepository.createSession(userId, now);
        sessionRepository.addSnapshot(sessionId, "Image 0", 0.0, 0.0, 0.0, 0.0, 0, analysis, now);
        return sessionId;
    }

    public Session getOwnedSessionOrThrow(int sessionId, long userId) {
        Session session = sessionRepository.findOwnedSession(sessionId, userId);
        if (session != null) {
            return session;
        }
        if (sessionRepository.existsSession(sessionId)) {
            throw new SecurityException("You do not have access to this session.");
        }
        throw new IllegalArgumentException("Session with id " + sessionId + " does not exist.");
    }

    public List<SessionRepository.StoredSnapshot> getPendingOwnedSnapshotsOrThrow(int sessionId, long userId) {
        if (!sessionRepository.isSessionOwnedByUser(sessionId, userId)) {
            if (sessionRepository.existsSession(sessionId)) {
                throw new SecurityException("You do not have access to this session.");
            }
            throw new IllegalArgumentException("Session with id " + sessionId + " does not exist.");
        }
        return sessionRepository.findPendingOwnedSnapshots(sessionId, userId);
    }

    public void updateSnapshotAnalysis(long snapshotId, com.fasterxml.jackson.databind.JsonNode analysis) {
        sessionRepository.updateSnapshotAnalysis(snapshotId, analysis);
    }

    public List<Integer> getOwnedSessionIds(long userId) {
        return sessionRepository.findSessionIdsByUserId(userId);
    }

	public void deleteSession(int sessionId, long userId) throws SecurityException, IllegalArgumentException {
		if (!sessionRepository.isSessionOwnedByUser(sessionId, userId)) {
			if (sessionRepository.existsSession(sessionId)) {
				throw new SecurityException("You do not have access to this session.");
			}
			throw new IllegalArgumentException("Session with id " + sessionId + " does not exist.");
		}
		sessionRepository.deleteSession(sessionId);
	}

    public String toJson(Session session) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("sessionId", session.sessionId());
        root.put("creationTime", session.creationTime().toString());
		ObjectNode snapshotNode = root.putObject("snapshot");
		Snapshot snapshot = session.snapshot();

		snapshotNode.put("label", snapshot.label());
		snapshotNode.put("minLng", snapshot.minLng());
		snapshotNode.put("minLat", snapshot.minLat());
		snapshotNode.put("maxLng", snapshot.maxLng());
		snapshotNode.put("maxLat", snapshot.maxLat());
		snapshotNode.put("usedZoom", snapshot.usedZoom());
		snapshotNode.put("creationTime", snapshot.creationTime().toString());
		if (snapshot.analysis() == null) {
			snapshotNode.putNull("analysis");
		} else {
			snapshotNode.set("analysis", snapshot.analysis());
		}

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize session JSON.", e);
        }
    }
}