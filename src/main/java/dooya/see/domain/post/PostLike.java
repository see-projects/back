package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends AbstractEntity {
    private Long postId;

    private Long memberId;

    private LocalDateTime likedAt;

    public static PostLike create(Long postId, Long memberId) {
        PostLike postLike = new PostLike();
        postLike.postId = requireNonNull(postId);
        postLike.memberId = requireNonNull(memberId);
        postLike.likedAt = LocalDateTime.now();

        return postLike;
    }
}
