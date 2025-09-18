package dooya.see.domain.post;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostStatusTest {
    @Test
    void 상태_이름이_올바르게_반환된다() {
        assertThat(PostStatus.DRAFT.name()).isEqualTo("DRAFT");
        assertThat(PostStatus.PUBLISHED.name()).isEqualTo("PUBLISHED");
        assertThat(PostStatus.HIDDEN.name()).isEqualTo("HIDDEN");
        assertThat(PostStatus.DELETED.name()).isEqualTo("DELETED");
    }

    @Test
    void 문자열로부터_PostStatus를_생성할_수_있다() {
        assertThat(PostStatus.valueOf("DRAFT")).isEqualTo(PostStatus.DRAFT);
        assertThat(PostStatus.valueOf("PUBLISHED")).isEqualTo(PostStatus.PUBLISHED);
        assertThat(PostStatus.valueOf("HIDDEN")).isEqualTo(PostStatus.HIDDEN);
        assertThat(PostStatus.valueOf("DELETED")).isEqualTo(PostStatus.DELETED);
    }

    @Test
    void 잘못된_문자열로_PostStatus_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> PostStatus.valueOf("INVALID_STATUS"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}