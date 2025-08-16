package dooya.see.domain.post;

public class UnauthorizedPostAccessException extends RuntimeException {
    public UnauthorizedPostAccessException(String message) {
        super(message);
    }
}
