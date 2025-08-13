package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProfileTest {
    @Test
    @DisplayName("프로필 주소가 null이면 예외가 발생한다")
    void profileNotNull() {
        assertThatThrownBy(() -> new Profile(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 주소가 유효하지 않으면 예외가 발생한다")
    void profileNotEmpty() {
        assertThatThrownBy(() -> new Profile("longlonglonglonglonglongprofile")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile("A")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile("한글 프로필")).isInstanceOf(IllegalArgumentException.class);
    }
}
