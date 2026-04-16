package olihef.stratvis.auth;

import jakarta.servlet.http.HttpServletRequest;

import static olihef.stratvis.auth.AuthExceptions.UnauthorizedException;

public final class AuthContext {
    public static final String REQUEST_ATTRIBUTE = "stratvis.auth.user";

    private AuthContext() {
    }

    public static AuthenticatedUser requireUser(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE);
        if (!(value instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Authentication required.");
        }
        return user;
    }
}
