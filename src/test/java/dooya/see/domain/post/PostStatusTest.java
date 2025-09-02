package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostStatusTest {
    @DisplayName("상태 이름이 올바르게 반환된다")
    @Test
    void returnsCorrectStatusName() {
        assertThat(PostStatus.DRAFT.name()).isEqualTo("DRAFT");
        assertThat(PostStatus.PUBLISHED.name()).isEqualTo("PUBLISHED");
        assertThat(PostStatus.HIDDEN.name()).isEqualTo("HIDDEN");
        assertThat(PostStatus.DELETED.name()).isEqualTo("DELETED");
    }

    @DisplayName("문자열로부터 PostStatus를 생성할 수 있다")
    @Test
    void canCreateFromString() {
        assertThat(PostStatus.valueOf("DRAFT")).isEqualTo(PostStatus.DRAFT);
        assertThat(PostStatus.valueOf("PUBLISHED")).isEqualTo(PostStatus.PUBLISHED);
        assertThat(PostStatus.valueOf("HIDDEN")).isEqualTo(PostStatus.HIDDEN);
        assertThat(PostStatus.valueOf("DELETED")).isEqualTo(PostStatus.DELETED);
    }

    @DisplayName("잘못된 문자열로 PostStatus 생성 시 예외가 발생한다")
    @Test
    void throwsExceptionForInvalidString() {
        assertThatThrownBy(() -> PostStatus.valueOf("INVALID_STATUS"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}