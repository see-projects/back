package dooya.see.domain.post;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public record CommentMetaData(
        LocalDateTime createdAt,

        LocalDateTime modifiedAt
) {
    public static CommentMetaData create() {
        LocalDateTime now = LocalDateTime.now();
        return new CommentMetaData(
                now,
                null
        );
    }
}
