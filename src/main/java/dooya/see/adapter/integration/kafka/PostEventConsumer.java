package dooya.see.adapter.integration.kafka;

import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.post.event.PostCreated;
import dooya.see.domain.post.event.PostUpdated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventConsumer {
    private final PostSearchIndexer postSearchIndexer;

    @KafkaListener(topics = "post-events", groupId = "post-indexr-group")
    public void consume(Object event) {
        log.info("Kafka Event 수신: {}", event);

        try {
            if (event instanceof PostCreated created) {
                postSearchIndexer.index(created.post());
            } else {
                log.warn("처리 불가능한 이벤트 타입: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Kafka 이벤트 처리 중 오류 발생: {}", event, e);
        }
    }
}
