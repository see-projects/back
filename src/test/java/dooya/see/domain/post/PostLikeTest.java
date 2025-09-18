package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {
    @Test
    void PostLike_생성_시_postId_memberId_likedAt이_올바르게_설정된다() {
        PostLike postLike = PostLike.create(1L, 1L);

        assertThat(postLike).isNotNull();
        assertThat(postLike.getPostId()).isEqualTo(1L);
        assertThat(postLike.getMemberId()).isEqualTo(1L);
        assertThat(postLike.getLikedAt()).isNotNull();
    }
}
