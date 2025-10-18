package dooya.see.adapter.integration.kafka;

import dooya.see.application.post.required.PostEventPublisher;
import dooya.see.domain.shared.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "see.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class DirectPostEventPublisher implements PostEventPublisher {
    private final PostEventProcessor postEventProcessor;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Kafka 비활성화 상태 - 도메인 이벤트를 직접 처리합니다: {}", event);
        postEventProcessor.process(event);
    }
}
