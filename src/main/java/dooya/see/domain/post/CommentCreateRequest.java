package dooya.see.domain.post;

public record CommentCreateRequest(
        String body,
        Long parentCommentId
) {
    public CommentCreateRequest(String body) {
        this(body, null);
    }

    public boolean isReply() {
        return parentCommentId != null;
    }
}
