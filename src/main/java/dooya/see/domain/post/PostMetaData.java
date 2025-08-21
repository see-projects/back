package dooya.see.domain.post;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public record PostMetaData(
        LocalDateTime createdAt,

        LocalDateTime modifiedAt,

        LocalDateTime publishedAt
) {
    
    public static PostMetaData create() {
        LocalDateTime now = LocalDateTime.now();
        return new PostMetaData(
                now,      // createdAt
                null,     // modifiedAt
                null
        );
    }

    public static PostMetaData createPublished() {
        LocalDateTime now = LocalDateTime.now();
        return new PostMetaData(
                now,      // createdAt
                null,     // modifiedAt
                now
        );
    }

    PostMetaData updateModifiedAt() {
        return new PostMetaData(
                this.createdAt,
                LocalDateTime.now(),
                this.publishedAt
        );
    }

    PostMetaData updatePublishedAt() {
        return new PostMetaData(
                this.createdAt,
                this.modifiedAt,
                LocalDateTime.now()
        );
    }

    public boolean isPublished() {
        return this.publishedAt != null;
    }

    public boolean isModified() {
        return this.modifiedAt != null;
    }

    public boolean isRecentlyCreated() {
        return this.createdAt.isAfter(LocalDateTime.now().minusDays(1));
    }

    public boolean isRecentlyModified() {
        return this.modifiedAt != null && this.modifiedAt.isAfter(LocalDateTime.now().minusDays(1));
    }
}
