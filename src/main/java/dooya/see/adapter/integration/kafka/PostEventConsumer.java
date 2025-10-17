package dooya.see.adapter.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "see.kafka.enabled", havingValue = "true")
public class PostEventConsumer {
    private final PostEventProcessor postEventProcessor;

    @KafkaListener(
            topics = "${see.kafka.topics.post-events:post-events}",
            groupId = "${spring.kafka.consumer.group-id:post-indexer-group}"
    )
    public void consume(PostEventMessage message) {
        log.debug("Kafka 이벤트 수신: {}", message);
        postEventProcessor.process(message);
    }
}
