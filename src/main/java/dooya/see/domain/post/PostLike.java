package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class PostLike extends AbstractEntity {
    private Long postId;

    private Long memberId;

    private LocalDateTime likedAt;

    public static PostLike create(long postId, long memberId) {
        return null;
    }
}
