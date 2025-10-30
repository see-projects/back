package dooya.see.application.post;

import dooya.see.application.post.required.PostSearchCacheRepository;
import dooya.see.domain.post.event.PostCreated;
import dooya.see.domain.post.event.PostDeleted;
import dooya.see.domain.post.event.PostHidden;
import dooya.see.domain.post.event.PostPublished;
import dooya.see.domain.post.event.PostUpdated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSearchCacheEventHandler {
    private final PostSearchCacheRepository cacheRepository;

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreated event) {
        evict("PostCreated", event.postId());
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostUpdated(PostUpdated event) {
        evict("PostUpdated", event.postId());
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostPublished(PostPublished event) {
        evict("PostPublished", event.postId());
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostHidden(PostHidden event) {
        evict("PostHidden", event.postId());
    }

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDeleted(PostDeleted event) {
        evict("PostDeleted", event.postId());
    }

    @Async("applicationTaskExecutor")
    @EventListener
    public void handleCacheEvictRequest(PostSearchCacheEvictCommand command) {
        evict(command.source(), command.postId());
    }

    private void evict(String source, Long postId) {
        log.debug("검색 캐시 무효화 요청 처리: source={}, postId={}", source, postId);
        try {
            cacheRepository.evictAll();
        } catch (Exception e) {
            log.error("검색 캐시 무효화 실패: source={}, postId={}", source, postId, e);
        }
    }

    public record PostSearchCacheEvictCommand(String source, Long postId) {
    }
}
