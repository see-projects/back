package dooya.see.domain.member;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileTest {
    private static final String VALID_ADDRESS = "user123";
    private static final String EMPTY_ADDRESS = "";
    private static final String MAX_LENGTH_ADDRESS = "123456789012345";
    private static final String TOO_LONG_ADDRESS = "longlonglonglonglonglongprofile";
    private static final String INVALID_UPPERCASE = "A";
    private static final String INVALID_KOREAN = "한글프로필";

    @Nested
    class 프로필_생성 {
        @Test
        void 올바른_형식의_주소로_생성할_수_있다() {
            Profile profile = new Profile(VALID_ADDRESS);

            assertThat(profile.address()).isEqualTo(VALID_ADDRESS);
        }

        @Test
        void 빈_문자열로_생성할_수_있다() {
            Profile profile = new Profile(EMPTY_ADDRESS);

            assertThat(profile.address()).isEqualTo(EMPTY_ADDRESS);
        }

        @Test
        void 최대_길이_15자로_생성할_수_있다() {
            Profile profile = new Profile(MAX_LENGTH_ADDRESS);

            assertThat(profile.address()).isEqualTo(MAX_LENGTH_ADDRESS);
        }
    }

    @Nested
    class 프로필_생성_실페 {
        @Test
        void null_주소는_예외가_발생한다() {
            assertThatThrownBy(() -> new Profile(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 주소가_15자를_초과하면_예외가_발생한다() {
            assertThatThrownBy(() -> new Profile(TOO_LONG_ADDRESS))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 잘못된_형식의_주소는_예외가_발생한다() {
            assertThatInvalidAddressThrowsException(INVALID_UPPERCASE);
            assertThatInvalidAddressThrowsException(INVALID_KOREAN);
        }

        private void assertThatInvalidAddressThrowsException(String invalidAddress) {
            assertThatThrownBy(() -> new Profile(invalidAddress))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
