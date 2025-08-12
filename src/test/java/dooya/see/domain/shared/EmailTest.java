package dooya.see.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    @Test
    @DisplayName("이메일 null 검증")
    void emailNotNull() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }

    @Test
    @DisplayName("이메일 빈 문자열 검증")
    void emailNotEmpty() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }

    @Test
    @DisplayName("이메일 잘못된 형식 검증")
    void emailValid() {
        assertThatThrownBy(() -> new Email("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }
}
