package dooya.see.domain.post.dto;

public record CommentUpdateRequest(
        String body
) {
    public boolean hasUpdate() {
        return body != null && !body.trim().isEmpty();
    }
}
