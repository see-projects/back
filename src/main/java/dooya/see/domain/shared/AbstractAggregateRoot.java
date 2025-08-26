package dooya.see.domain.shared;

import dooya.see.domain.AbstractEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAggregateRoot extends AbstractEntity {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected  void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }
}
