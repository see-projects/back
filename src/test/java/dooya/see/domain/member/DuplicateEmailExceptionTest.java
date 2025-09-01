package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateEmailExceptionTest {

    @Test
    @DisplayName("메시지와 함께 예외가 생성된다")
    void createExceptionWithMessage() {
        String errorMessage = "이미 존재하는 이메일입니다: test@example.com";

        DuplicateEmailException exception = new DuplicateEmailException(errorMessage);

        assertThat(exception.getMessage()).isEqualTo(errorMessage);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 메시지로 예외가 생성된다")
    void createExceptionWithNullMessage() {
        String errorMessage = null;
        
        DuplicateEmailException exception = new DuplicateEmailException(errorMessage);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("빈 메시지로 예외가 생성된다")
    void createExceptionWithEmptyMessage() {
        String errorMessage = "";

        DuplicateEmailException exception = new DuplicateEmailException(errorMessage);

        assertThat(exception.getMessage()).isEqualTo("");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
