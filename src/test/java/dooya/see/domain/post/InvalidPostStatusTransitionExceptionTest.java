package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidPostStatusTransitionExceptionTest {

    @DisplayName("현재 상태와 시도한 작업으로 예외가 생성된다")
    @Test
    void createExceptionWithCurrentStatusAndAction() {
        PostStatus currentStatus = PostStatus.PUBLISHED;
        String attemptedAction = "delete";

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(currentStatus, attemptedAction);

        assertThat(exception.getCurrentStatus()).isEqualTo(currentStatus);
        assertThat(exception.getAttemptedAction()).isEqualTo(attemptedAction);
        assertThat(exception.getMessage())
            .isEqualTo("현재 상태 'PUBLISHED'에서 'delete' 작업을 수행할 수 없습니다");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @DisplayName("모든 PostStatus에 대해 예외가 생성된다")
    @ParameterizedTest
    @EnumSource(PostStatus.class)
    void createExceptionWithAllPostStatuses(PostStatus status) {
        String attemptedAction = "invalidAction";

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(status, attemptedAction);

        assertThat(exception.getCurrentStatus()).isEqualTo(status);
        assertThat(exception.getAttemptedAction()).isEqualTo(attemptedAction);
        assertThat(exception.getMessage())
            .contains(status.toString())
            .contains(attemptedAction);
    }

    @DisplayName("다양한 작업 이름으로 예외가 생성된다")
    @ParameterizedTest
    @ValueSource(strings = {"publish", "hide", "delete", "edit", "archive"})
    void createExceptionWithVariousActions(String action) {
        PostStatus currentStatus = PostStatus.DRAFT;

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(currentStatus, action);

        assertThat(exception.getCurrentStatus()).isEqualTo(currentStatus);
        assertThat(exception.getAttemptedAction()).isEqualTo(action);
        assertThat(exception.getMessage())
            .contains("DRAFT")
            .contains(action);
    }

    @DisplayName("null 값들로 예외가 생성된다")
    @Test
    void createExceptionWithNullValues() {
        PostStatus currentStatus = null;
        String attemptedAction = null;

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(currentStatus, attemptedAction);

        assertThat(exception.getCurrentStatus()).isNull();
        assertThat(exception.getAttemptedAction()).isNull();
        assertThat(exception.getMessage())
            .isEqualTo("현재 상태 'null'에서 'null' 작업을 수행할 수 없습니다");
    }

    @DisplayName("메시지 포맷이 올바르게 생성된다")
    @Test
    void messageFormatIsCorrect() {
        PostStatus currentStatus = PostStatus.HIDDEN;
        String attemptedAction = "publish";

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(currentStatus, attemptedAction);

        String expectedMessage = String.format("현재 상태 '%s'에서 '%s' 작업을 수행할 수 없습니다", 
                                              currentStatus, attemptedAction);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }
}
