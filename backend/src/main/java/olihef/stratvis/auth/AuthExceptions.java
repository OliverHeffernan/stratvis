package olihef.stratvis.auth;

public final class AuthExceptions {
    private AuthExceptions() {
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
