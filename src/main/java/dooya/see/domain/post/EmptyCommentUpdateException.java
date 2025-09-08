package dooya.see.domain.post;

public class EmptyCommentUpdateException extends RuntimeException {
    public EmptyCommentUpdateException(String message) {
        super(message);
    }

    public EmptyCommentUpdateException() {
        super("수정할 내용이 없습니다");
    }
}
