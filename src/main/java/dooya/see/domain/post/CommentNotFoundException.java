package dooya.see.domain.post;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }

    public CommentNotFoundException(Long commentId) {
        super("댓글을 찾을 수 없습니다: " + commentId);
    }
}
