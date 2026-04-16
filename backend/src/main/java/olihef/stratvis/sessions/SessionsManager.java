package olihef.stratvis.sessions;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;

public class SessionsManager {
	private static Map<Integer, Session> sessions = new HashMap<>();
	private static int lastSession = 0;

	public static int addNewSession() {
		lastSession++;
		sessions.put(lastSession, new Session(lastSession));
		return lastSession;
	}
	public static int addNewSessionWithImage(String image) {
		throw new UnsupportedOperationException("Image-only session creation is no longer supported.");
	}

	public static int addNewSessionWithSnapshot(double minLng, double minLat, double maxLng, double maxLat, int usedZoom) {
		lastSession++;
		Session session = new Session(lastSession).addSnapshot(minLng, minLat, maxLng, maxLat, usedZoom);
		sessions.put(lastSession, session);
		return lastSession;
	}

	public static int addNewSessionWithImageAndAnalysis(MultipartFile image, JsonNode analysis) {
		lastSession++;
		Session session = new Session(lastSession)
			.addSnapshot(0.0, 0.0, 0.0, 0.0, 0)
			.setAnalysis(analysis, 0);
		sessions.put(lastSession, session);
		return lastSession;
	}
	public static Session getSession(int sessionId) throws IllegalStateException {
		Session session = sessions.get(sessionId);
		if (session == null) {
			throw new IllegalArgumentException("Session with id " + sessionId + " does not exist.");
		}
		return session;
	}
}
