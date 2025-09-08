package dooya.see.domain.post;

public class UnauthorizedCommentAccessException extends RuntimeException {
    public UnauthorizedCommentAccessException(String message) {
        super(message);
    }

    public static UnauthorizedCommentAccessException forAction(String action) {
        return new UnauthorizedCommentAccessException("댓글 작성자만 " + action + "할 수 있습니다");
    }
}
