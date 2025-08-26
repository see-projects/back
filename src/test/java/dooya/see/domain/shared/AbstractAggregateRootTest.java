package dooya.see.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractAggregateRootTest {
    @Test
    @DisplayName("도메인 이벤트를 추가하면 이벤트 목록이 저장된다")
    void addDomainEvent() {
        TestAggregateRoot aggregateRoot = new TestAggregateRoot();
        TestDomainEvent event = new TestDomainEvent("test");

        aggregateRoot.addTestEvent(event);

        assertThat(aggregateRoot.getDomainEvents()).hasSize(1);
        assertThat(aggregateRoot.getDomainEvents().getFirst()).isEqualTo(event);
        assertThat(aggregateRoot.hasDomainEvents()).isTrue();
    }

    @Test
    @DisplayName("도메인 이벤트를 클리어하면 이벤트 목록이 비어진다")
    void clearDomainEvents() {
        TestAggregateRoot aggregateRoot = new TestAggregateRoot();
        aggregateRoot.addTestEvent(new TestDomainEvent("test"));

        aggregateRoot.clearDomainEvents();

        assertThat(aggregateRoot.getDomainEvents()).isEmpty();
        assertThat(aggregateRoot.hasDomainEvents()).isFalse();
    }

    @Test
    @DisplayName("도메인 이벤트를 추가하면 순서대로 저장된다")
    void c() {
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
