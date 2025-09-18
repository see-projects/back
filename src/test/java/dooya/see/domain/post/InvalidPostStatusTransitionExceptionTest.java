package dooya.see.domain.post;

import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidPostStatusTransitionExceptionTest {
    @Test
    void 현재_상태와_시도한_작업으로_예외가_생성된다() {
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

    @ParameterizedTest
    @EnumSource(PostStatus.class)
    void 모든_PostStatus에_대해_예외가_생성된다(PostStatus status) {
        String attemptedAction = "invalidAction";

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(status, attemptedAction);

        assertThat(exception.getCurrentStatus()).isEqualTo(status);
        assertThat(exception.getAttemptedAction()).isEqualTo(attemptedAction);
        assertThat(exception.getMessage())
            .contains(status.toString())
            .contains(attemptedAction);
    }

    @ParameterizedTest
    @ValueSource(strings = {"publish", "hide", "delete", "edit", "archive"})
    void 다양한_작업_이름으로_예외가_생성된다(String action) {
        PostStatus currentStatus = PostStatus.DRAFT;

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(currentStatus, action);

        assertThat(exception.getCurrentStatus()).isEqualTo(currentStatus);
        assertThat(exception.getAttemptedAction()).isEqualTo(action);
        assertThat(exception.getMessage())
            .contains("DRAFT")
            .contains(action);
    }

    @Test
    void null_값들로_예외가_생성된다() {
        PostStatus currentStatus = null;
        String attemptedAction = null;

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(currentStatus, attemptedAction);

        assertThat(exception.getCurrentStatus()).isNull();
        assertThat(exception.getAttemptedAction()).isNull();
        assertThat(exception.getMessage())
            .isEqualTo("현재 상태 'null'에서 'null' 작업을 수행할 수 없습니다");
    }

    @Test
    void 메시지_포맷이_올바르게_생성된다() {
        PostStatus currentStatus = PostStatus.HIDDEN;
        String attemptedAction = "publish";

        InvalidPostStatusTransitionException exception = 
            new InvalidPostStatusTransitionException(currentStatus, attemptedAction);

        String expectedMessage = String.format("현재 상태 '%s'에서 '%s' 작업을 수행할 수 없습니다", 
                                              currentStatus, attemptedAction);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }
}
