package dooya.see.domain.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostLikeTest {
    private static final Long POST_ID = 100L;
    private static final Long MEMBER_ID = 1L;

    @Nested
    class 좋아요_생성 {
        @Test
        void 생성_시_모든_필드가_올바르게_설정된다() {
            LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);

            PostLike postLike = PostLike.create(POST_ID, MEMBER_ID);

            LocalDateTime afterCreate = LocalDateTime.now().plusSeconds(1);

            assertThatPostLikeCreatedCorrectly(postLike, beforeCreate, afterCreate);
        }

        @Test
        void postId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> PostLike.create(null, MEMBER_ID))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void memberId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> PostLike.create(POST_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }

        private void assertThatPostLikeCreatedCorrectly(PostLike postLike, LocalDateTime beforeCreate, LocalDateTime afterCreate) {
            assertThat(postLike).isNotNull();
            assertThat(postLike.getPostId()).isEqualTo(POST_ID);
            assertThat(postLike.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(postLike.getLikedAt()).isBetween(beforeCreate, afterCreate);
        }
    }
}