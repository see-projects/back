package dooya.see.domain.post;

public record CommentUpdateRequest(
        String body
) {
    public boolean hasUpdate() {
        return body != null && !body.trim().isEmpty();
    }
}
