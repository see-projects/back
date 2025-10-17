package dooya.see.adapter.integration.kafka;

import dooya.see.application.post.required.PostEventPublisher;
import dooya.see.domain.shared.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventProducer implements PostEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "post-events";

    @Override
    public void publish(DomainEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event);
            log.info("Kafka에 이벤트 발행 성공: {}", event);
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패: {}", event, e);
        }
    }
}
