package dooya.see.domain.post.exception;

import dooya.see.domain.post.CommentStatus;

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

    public static InvalidCommentStatusException forOperation(CommentStatus prohibitedStatus, String operation) {
        String message = String.format(
                "댓글 상태가 '%s'이므로 '%s'을(를) 수행할 수 없습니다",
                getStatusDescription(prohibitedStatus),
                operation
        );
        return new InvalidCommentStatusException(message);
    }

    private static String getStatusDescription(CommentStatus status) {
        return switch (status) {
            case ACTIVE -> "활성";
            case DELETED -> "삭제됨";
            case HIDDEN -> "숨김";
        };
    }
}
