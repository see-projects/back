package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostStats extends AbstractEntity {
    private Long postId;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    public static PostStats create(Long postId) {
        PostStats status = new PostStats();
        status.postId = Objects.requireNonNull(postId);
        status.viewCount = 0;
        status.likeCount = 0;
        status.commentCount = 0;

        return status;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public boolean isPopular() {
        return viewCount >= 1000 || likeCount >= 100;
    }

    public boolean isViral() {
        return viewCount >= 10000 || likeCount >= 1000;
    }

    public boolean hasEngagement() {
        return likeCount > 0 || commentCount > 0;
    }

    public double getEngagementRate() {
        if (viewCount == 0) return 0.0;
        return (double) (likeCount + commentCount) / viewCount * 100;
    }

    public boolean hasValidStats() {
        return viewCount >= 0 && likeCount >= 0 && commentCount >= 0;
    }
}
