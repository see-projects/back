package dooya.see.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    @Test
    @DisplayName("동일한 이메일 주소를 가진 Email 객체들은 동등하다")
    void equality() {
        var email1 = new Email("dooya@see.com");
        var email2 = new Email("dooya@see.com");

        assertThat(email1).isEqualTo(email2);
    }

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
