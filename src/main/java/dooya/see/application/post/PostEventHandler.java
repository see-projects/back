package dooya.see.application.post;

import dooya.see.application.post.required.PostEventPublisher;
import dooya.see.domain.post.event.PostCreated;
import dooya.see.domain.post.event.PostDeleted;
import dooya.see.domain.post.event.PostUpdated;
import dooya.see.domain.shared.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventHandler {
    private final PostEventPublisher postEventPublisher;

    @EventListener
    public void handlePostCreated(PostCreated event) {
        publish(event);
    }

    @EventListener
    public void handlePostUpdated(PostUpdated event) {
        publish(event);
    }

    @EventListener
    public void handlePostDeleted(PostDeleted event) {
        publish(event);
    }

    private void publish(DomainEvent event) {
        log.debug("도메인 이벤트를 외부 파이프라인으로 위임: {}", event);
        postEventPublisher.publish(event);
    }
}
