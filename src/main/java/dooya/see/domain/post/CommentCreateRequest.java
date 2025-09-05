package dooya.see.domain.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank @Size(max = 1000)
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
