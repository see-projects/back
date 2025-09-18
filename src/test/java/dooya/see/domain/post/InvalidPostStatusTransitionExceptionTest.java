package dooya.see.domain.post;

import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidPostStatusTransitionExceptionTest {
    private static final PostStatus CURRENT_STATUS = PostStatus.PUBLISHED;
    private static final String ATTEMPTED_ACTION = "delete";

    @Nested
    class 예외_생성 {
        @Test
        void 현재_상태와_시도한_작업으로_예외가_생성된다() {
            InvalidPostStatusTransitionException exception =
                    new InvalidPostStatusTransitionException(CURRENT_STATUS, ATTEMPTED_ACTION);

            assertThatExceptionCreatedCorrectly(exception, CURRENT_STATUS, ATTEMPTED_ACTION);
        }

        @Test
        void null_값들로도_예외가_생성된다() {
            InvalidPostStatusTransitionException exception =
                    new InvalidPostStatusTransitionException(null, null);

            assertThat(exception.getCurrentStatus()).isNull();
            assertThat(exception.getAttemptedAction()).isNull();
            assertThat(exception.getMessage()).isEqualTo("현재 상태 'null'에서 'null' 작업을 수행할 수 없습니다");
        }

        private void assertThatExceptionCreatedCorrectly(InvalidPostStatusTransitionException exception,
                                                         PostStatus expectedStatus, String expectedAction) {
            assertThat(exception.getCurrentStatus()).isEqualTo(expectedStatus);
            assertThat(exception.getAttemptedAction()).isEqualTo(expectedAction);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    class 메시지_생성 {
        @Test
        void 메시지_포맷이_올바르게_생성된다() {
            InvalidPostStatusTransitionException exception =
                    new InvalidPostStatusTransitionException(PostStatus.HIDDEN, "publish");

            String expectedMessage = "현재 상태 'HIDDEN'에서 'publish' 작업을 수행할 수 없습니다";
            assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        }

        @ParameterizedTest
        @EnumSource(PostStatus.class)
        void 모든_상태에_대해_메시지가_생성된다(PostStatus status) {
            InvalidPostStatusTransitionException exception =
                    new InvalidPostStatusTransitionException(status, "testAction");

            assertThatMessageContainsStatusAndAction(exception, status.toString(), "testAction");
        }

        @ParameterizedTest
        @ValueSource(strings = {"publish", "hide", "delete", "edit"})
        void 다양한_작업_이름으로_메시지가_생성된다(String action) {
            InvalidPostStatusTransitionException exception =
                    new InvalidPostStatusTransitionException(PostStatus.DRAFT, action);

            assertThatMessageContainsStatusAndAction(exception, "DRAFT", action);
        }

        private void assertThatMessageContainsStatusAndAction(InvalidPostStatusTransitionException exception,
                                                              String expectedStatus, String expectedAction) {
            assertThat(exception.getMessage())
                    .contains(expectedStatus)
                    .contains(expectedAction);
        }
    }

    @Nested
    class 예외_특성 {
        @Test
        void RuntimeException을_상속한다() {
            InvalidPostStatusTransitionException exception =
                    new InvalidPostStatusTransitionException(CURRENT_STATUS, ATTEMPTED_ACTION);

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        void 동일한_정보로_생성된_예외는_같은_메시지를_가진다() {
            InvalidPostStatusTransitionException exception1 =
                    new InvalidPostStatusTransitionException(CURRENT_STATUS, ATTEMPTED_ACTION);
            InvalidPostStatusTransitionException exception2 =
                    new InvalidPostStatusTransitionException(CURRENT_STATUS, ATTEMPTED_ACTION);

            assertThat(exception1.getMessage()).isEqualTo(exception2.getMessage());
            assertThat(exception1.getCurrentStatus()).isEqualTo(exception2.getCurrentStatus());
            assertThat(exception1.getAttemptedAction()).isEqualTo(exception2.getAttemptedAction());
        }
    }
}