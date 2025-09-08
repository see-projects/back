package dooya.see.domain.post;

public class InvalidCommentStatusException extends RuntimeException {
    public InvalidCommentStatusException(String message) {
        super(message);
    }

    public static InvalidCommentStatusException cannotModify() {
        return new InvalidCommentStatusException("수정할 수 없는 댓글입니다");
    }

    public static InvalidCommentStatusException alreadyHidden() {
        return new InvalidCommentStatusException("이미 숨김 처리된 댓글입니다");
    }

    public static InvalidCommentStatusException cannotHideDeleted() {
        return new InvalidCommentStatusException("삭제된 댓글을 숨김 처리할 수 없습니다");
    }
}
