package dooya.see.domain.post.exception;

public class DuplicateLikeException extends RuntimeException {
    public DuplicateLikeException(String message) {
        super(message);
    }

    public DuplicateLikeException(String message, Throwable cause) {
        super(message, cause);
    }
}
