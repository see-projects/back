package dooya.see.domain.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    @Test
    void 동일한_이메일_주소를_가진_Email_객체들은_동등하다() {
        var email1 = new Email("dooya@see.com");
        var email2 = new Email("dooya@see.com");

        assertThat(email1).isEqualTo(email2);
    }

    @Test
    void 이메일이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }

    @Test
    void 이메일이_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }

    @Test
    void 이메일_형식이_올바르지_않으면_예외가_발생한다() {
        assertThatThrownBy(() -> new Email("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식이 바르지 않습니다");
    }
}
