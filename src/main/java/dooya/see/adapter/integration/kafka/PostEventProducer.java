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
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "see.kafka.enabled", havingValue = "true")
public class PostEventProducer implements PostEventPublisher {
    private final KafkaTemplate<String, PostEventMessage> kafkaTemplate;
    private final RetryTemplate retryTemplate;

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

        String key = message.postId() != null ? message.postId().toString() : null;

        Runnable sendTask = () -> {
            try {
                sendWithRetry(key, message);
            } catch (Exception unexpected) {
                log.error("Kafka 이벤트 발행 중 처리되지 않은 예외 발생: {}", message, unexpected);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendTask.run();
                }
            });
        } else {
            sendTask.run();
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

    private void sendWithRetry(String key, PostEventMessage message) {
        retryTemplate.execute(retryContext -> {
            try {
                var result = kafkaTemplate.send(topic, key, message).get();
                if (result != null && log.isDebugEnabled()) {
                    log.debug("Kafka 이벤트 발행 성공: topic={}, partition={}, offset={}, key={}, payload={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key,
                            message);
                }
                return null;
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Kafka 전송이 인터럽트되었습니다", interrupted);
            } catch (ExecutionException executionException) {
                Throwable cause = executionException.getCause() != null ? executionException.getCause() : executionException;
                throw new IllegalStateException("Kafka 전송 실패", cause);
            }
        }, recoveryContext -> {
            Throwable lastError = recoveryContext.getLastThrowable();
            log.error("Kafka 이벤트 발행 실패(재시도 완료): {}", message, lastError);
            // TODO: DLQ 또는 모니터링 알림 연동을 고려합니다.
            return null;
        });
    }
}
