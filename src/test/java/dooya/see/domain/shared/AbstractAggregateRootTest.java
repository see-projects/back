package dooya.see.domain.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractAggregateRootTest {
    @Test
    void 도메인_이벤트를_추가하면_이벤트_목록이_저장된다() {
        TestAggregateRoot aggregateRoot = new TestAggregateRoot();
        TestDomainEvent event = new TestDomainEvent("test");

        aggregateRoot.addTestEvent(event);

        assertThat(aggregateRoot.getDomainEvents()).hasSize(1);
        assertThat(aggregateRoot.getDomainEvents().getFirst()).isEqualTo(event);
        assertThat(aggregateRoot.hasDomainEvents()).isTrue();
    }

    @Test
    void 도메인_이벤트를_클리어하면_이벤트_목록이_비어진다() {
        TestAggregateRoot aggregateRoot = new TestAggregateRoot();
        aggregateRoot.addTestEvent(new TestDomainEvent("test"));

        aggregateRoot.clearDomainEvents();

        assertThat(aggregateRoot.getDomainEvents()).isEmpty();
        assertThat(aggregateRoot.hasDomainEvents()).isFalse();
    }

    @Test
    void 도메인_이벤트를_추가하면_순서대로_저장된다() {
        TestAggregateRoot aggregateRoot = new TestAggregateRoot();
        TestDomainEvent event1 = new TestDomainEvent("first");
        TestDomainEvent event2 = new TestDomainEvent("second");

        aggregateRoot.addTestEvent(event1);
        aggregateRoot.addTestEvent(event2);

        assertThat(aggregateRoot.getDomainEvents()).hasSize(2);
        assertThat(aggregateRoot.getDomainEvents().getFirst()).isEqualTo(event1);
        assertThat(aggregateRoot.getDomainEvents().getLast()).isEqualTo(event2);
    }

    private static class TestAggregateRoot extends AbstractAggregateRoot{
        public void addTestEvent(TestDomainEvent event) {
            addDomainEvent(event);
        }
    }

    private record TestDomainEvent(String message) implements DomainEvent {}
}
