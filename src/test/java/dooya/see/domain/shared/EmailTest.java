package dooya.see.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    @Test
    @DisplayName("이메일이 null이면 예외가 발생한다")
    void emailNotNull() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }

    @Test
    @DisplayName("이메일이 빈 문자열이면 예외가 발생한다")
    void emailNotEmpty() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 예외가 발생한다")
    void emailValid() {
        assertThatThrownBy(() -> new Email("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }
}
