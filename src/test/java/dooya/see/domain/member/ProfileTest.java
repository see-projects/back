package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProfileTest {
    @Test
    @DisplayName("프로필 null 검증")
    void profileNotNull() {
        assertThatThrownBy(() -> new Profile(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 문자열 검증")
    void profileNotEmpty() {
        assertThatThrownBy(() -> new Profile("longlonglonglonglonglongprofile")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile("A")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile("한글 프로필")).isInstanceOf(IllegalArgumentException.class);
    }
}
