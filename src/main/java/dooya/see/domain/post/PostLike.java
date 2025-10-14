package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Entity
@Table(name = "post_like", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_like_member_post", columnNames = {"post_id", "member_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends AbstractEntity {
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "liked_at", nullable = false)
    private LocalDateTime likedAt;

    public static PostLike create(Long postId, Long memberId) {
        PostLike postLike = new PostLike();
        postLike.postId = requireNonNull(postId);
        postLike.memberId = requireNonNull(memberId);
        postLike.likedAt = LocalDateTime.now();

        return postLike;
    }
}
