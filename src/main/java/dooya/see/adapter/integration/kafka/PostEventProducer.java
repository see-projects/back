package dooya.see.adapter.integration.kafka;

import dooya.see.application.post.required.PostEventPublisher;
import dooya.see.domain.post.event.PostCreated;
import dooya.see.domain.post.event.PostDeleted;
import dooya.see.domain.post.event.PostUpdated;
import dooya.see.domain.shared.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "see.kafka.enabled", havingValue = "true")
public class PostEventProducer implements PostEventPublisher {
    private final KafkaTemplate<String, PostEventMessage> kafkaTemplate;

    @Value("${see.kafka.topics.post-events:post-events}")
    private String topic;

    @Override
    public void publish(DomainEvent event) {
        PostEventMessage message = toMessage(event);
        if (message == null) {
            if (log.isDebugEnabled()) {
                log.debug("Kafka 전송 대상이 아닌 이벤트입니다: {}", event.getClass().getName());
            }
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(message);
                }
            });
        } else {
            doSend(message);
        }
    }

    private PostEventMessage toMessage(DomainEvent event) {
        if (event instanceof PostCreated created) {
            return PostEventMessage.created(created.postId());
        }
        if (event instanceof PostUpdated updated) {
            return PostEventMessage.updated(updated.postId());
        }
        if (event instanceof PostDeleted deleted) {
            return PostEventMessage.deleted(deleted.postId());
        }
        return null;
    }

    private void doSend(PostEventMessage message) {
        try {
            kafkaTemplate.send(topic, message)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.error("Kafka 이벤트 발행 실패: {}", message, throwable);
                            return;
                        }
                        if (result != null && log.isDebugEnabled()) {
                            log.debug("Kafka 이벤트 발행 성공: topic={}, partition={}, offset={}, payload={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(),
                                    message);
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("Kafka 이벤트 발행 실패: {}", message, throwable);
                        return null;
                    });
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패: {}", message, e);
        }
    }
}
