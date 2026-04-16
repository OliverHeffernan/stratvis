package olihef.stratvis.sessions;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;
import java.io.IOException;
public class SessionsManager {
	private static Map<Integer, Session> sessions = new HashMap<>();
	private static int lastSession = 0;

	public static int addNewSession() {
		lastSession++;
		sessions.put(lastSession, new Session(lastSession));
		return lastSession;
	}
	public static int addNewSessionWithImage(String image) {
		lastSession++;
		Session session = new Session(lastSession).addImage(image);
		sessions.put(lastSession, session);
		return lastSession;
	}
	public static int addNewSessionWithImageAndAnalysis(MultipartFile image, JsonNode analysis) throws IOException {
		lastSession++;
		String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
		Session session = new Session(lastSession).addImage(base64Image).setAnalysis(analysis, 0);
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