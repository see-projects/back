package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {
    @DisplayName("PostLike 생성 시 postId, memberId, likedAt이 올바르게 설정된다")
    @Test
    void create_setsAllRequiredFields() {
        PostLike postLike = PostLike.create(1L, 1L);

        assertThat(postLike).isNotNull();
        assertThat(postLike.getPostId()).isEqualTo(1L);
        assertThat(postLike.getMemberId()).isEqualTo(1L);
        assertThat(postLike.getLikedAt()).isNotNull();
    }
}
