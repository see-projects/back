package dooya.see.application.post.required;

import dooya.see.domain.shared.DomainEvent;

public interface PostEventPublisher {
    void publish(DomainEvent event);
}
