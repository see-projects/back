package dooya.see.domain.post.exception;

public class UnauthorizedPostAccessException extends RuntimeException {
    public UnauthorizedPostAccessException(String message) {
        super(message);
    }
}
