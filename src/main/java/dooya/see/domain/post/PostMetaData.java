package dooya.see.domain.post;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public record PostMetaData(
        LocalDateTime createdAt,

        LocalDateTime modifiedAt,

        LocalDateTime publishedAt,

        Integer viewCount,

        Integer likeCount,

        Integer commentCount
) {
    
    public static PostMetaData create() {
        LocalDateTime now = LocalDateTime.now();
        return new PostMetaData(
                now,      // createdAt
                null,     // modifiedAt
                null,     // publishedAt
                0,        // viewCount
                0,        // likeCount
                0         // commentCount
        );
    }

    public PostMetaData updateModifiedAt() {
        return new PostMetaData(
                this.createdAt,
                LocalDateTime.now(),
                this.publishedAt,
                this.viewCount,
                this.likeCount,
                this.commentCount
        );
    }

    public PostMetaData updatePublishedAt() {
        return new PostMetaData(
                this.createdAt,
                this.modifiedAt,
                LocalDateTime.now(),
                this.viewCount,
                this.likeCount,
                this.commentCount
        );
    }

    public PostMetaData incrementViewCount() {
        return new PostMetaData(
                this.createdAt,
                this.modifiedAt,
                this.publishedAt,
                this.viewCount + 1,
                this.likeCount,
                this.commentCount
        );
    }

    public PostMetaData incrementLikeCount() {
        return new PostMetaData(
                this.createdAt,
                this.modifiedAt,
                this.publishedAt,
                this.viewCount,
                this.likeCount + 1,
                this.commentCount
        );
    }

    public PostMetaData incrementCommentCount() {
        return new PostMetaData(
                this.createdAt,
                this.modifiedAt,
                this.publishedAt,
                this.viewCount,
                this.likeCount,
                this.commentCount + 1
        );
    }
}
