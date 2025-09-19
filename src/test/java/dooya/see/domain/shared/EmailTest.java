package dooya.see.domain.shared;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    private static final String VALID_EMAIL = "dooya@see.com";
    private static final String ANOTHER_VALID_EMAIL = "user@example.org";

    @Nested
    class 이메일_생성 {
        @Test
        void 유효한_이메일_주소로_생성할_수_있다() {
            Email email = new Email(VALID_EMAIL);

            assertThat(email.address()).isEqualTo(VALID_EMAIL);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user123@domain.org",
                "name.surname@company.co.kr",
                "simple@test.io"
        })
        void 다양한_형식의_유효한_이메일로_생성할_수_있다(String validEmail) {
            Email email = new Email(validEmail);

            assertThat(email.address()).isEqualTo(validEmail);
        }
    }

    @Nested
    class 이메일_생성_실패 {
        @Test
        void null_이메일로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new Email(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일 형식이 바르지 않습니다");
        }

        @Test
        void 빈_문자열로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new Email(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일 형식이 바르지 않습니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "@example.com",
                "user@",
                "user name@example.com",
                "user@domain",
                "user@@example.com"
        })
        void 잘못된_형식의_이메일로_생성_시_예외가_발생한다(String invalidEmail) {
            assertThatThrownBy(() -> new Email(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일 형식이 바르지 않습니다");
        }
    }

    @Nested
    class 값_객체_특성 {
        @Test
        void 동일한_이메일_주소를_가진_객체는_같다() {
            Email email1 = new Email(VALID_EMAIL);
            Email email2 = new Email(VALID_EMAIL);

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        void 다른_이메일_주소를_가진_객체는_다르다() {
            Email email1 = new Email(VALID_EMAIL);
            Email email2 = new Email(ANOTHER_VALID_EMAIL);

            assertThat(email1).isNotEqualTo(email2);
        }

        @Test
        void 대소문자_구분하여_비교한다() {
            Email lowerCase = new Email("test@example.com");
            Email upperCase = new Email("TEST@EXAMPLE.COM");

            assertThat(lowerCase).isNotEqualTo(upperCase);
        }
    }
}