package dooya.see.domain.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractAggregateRootTest {
    private TestAggregateRoot aggregateRoot;

    @BeforeEach
    void setUp() {
        aggregateRoot = new TestAggregateRoot();
    }

    @Nested
    class 도메인_이벤트_추가 {
        @Test
        void 이벤트_추가_시_이벤트_목록에_저장된다() {
            TestDomainEvent event = new TestDomainEvent("test");

            aggregateRoot.addTestEvent(event);

            assertThatEventAdded(event);
        }

        @Test
        void 여러_이벤트_추가_시_순서대로_저장된다() {
            TestDomainEvent firstEvent = new TestDomainEvent("first");
            TestDomainEvent secondEvent = new TestDomainEvent("second");

            aggregateRoot.addTestEvent(firstEvent);
            aggregateRoot.addTestEvent(secondEvent);

            assertThatEventsAddedInOrder(firstEvent, secondEvent);
        }

        private void assertThatEventAdded(TestDomainEvent expectedEvent) {
            assertThat(aggregateRoot.getDomainEvents()).hasSize(1);
            assertThat(aggregateRoot.getDomainEvents().getFirst()).isEqualTo(expectedEvent);
            assertThat(aggregateRoot.hasDomainEvents()).isTrue();
        }

        private void assertThatEventsAddedInOrder(TestDomainEvent firstEvent, TestDomainEvent secondEvent) {
            assertThat(aggregateRoot.getDomainEvents()).hasSize(2);
            assertThat(aggregateRoot.getDomainEvents().getFirst()).isEqualTo(firstEvent);
            assertThat(aggregateRoot.getDomainEvents().getLast()).isEqualTo(secondEvent);
        }
    }

    @Nested
    class 도메인_이벤트_클리어 {
        @Test
        void 이벤트_클리어_시_이벤트_목록이_비어진다() {
            aggregateRoot.addTestEvent(new TestDomainEvent("test"));

            aggregateRoot.clearDomainEvents();

            assertThatEventsCleared();
        }

        @Test
        void 빈_이벤트_목록을_클리어해도_문제없다() {
            aggregateRoot.clearDomainEvents();

            assertThatEventsCleared();
        }

        private void assertThatEventsCleared() {
            assertThat(aggregateRoot.getDomainEvents()).isEmpty();
            assertThat(aggregateRoot.hasDomainEvents()).isFalse();
        }
    }

    @Nested
    class 도메인_이벤트_조회 {
        @Test
        void 초기_상태에서는_이벤트가_없다() {
            assertThat(aggregateRoot.hasDomainEvents()).isFalse();
            assertThat(aggregateRoot.getDomainEvents()).isEmpty();
        }

        @Test
        void 이벤트_목록은_수정_불가능하다() {
            aggregateRoot.addTestEvent(new TestDomainEvent("test"));

            assertThat(aggregateRoot.getDomainEvents()).isNotEmpty();
        }
    }

    @Nested
    class 애그리거트_루트_특성 {
        @Test
        void 동일한_이벤트를_여러_번_추가할_수_있다() {
            TestDomainEvent sameEvent = new TestDomainEvent("same");

            aggregateRoot.addTestEvent(sameEvent);
            aggregateRoot.addTestEvent(sameEvent);

            assertThat(aggregateRoot.getDomainEvents()).hasSize(2);
            assertThat(aggregateRoot.getDomainEvents()).allMatch(event -> event.equals(sameEvent));
        }
    }

    // 테스트용 클래스들

    private static class TestAggregateRoot extends AbstractAggregateRoot {
        public void addTestEvent(TestDomainEvent event) {
            addDomainEvent(event);
        }
    }

    private record TestDomainEvent(String message) implements DomainEvent {}
}