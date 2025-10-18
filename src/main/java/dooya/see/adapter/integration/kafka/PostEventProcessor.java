package dooya.see.adapter.integration.kafka;

import dooya.see.application.post.required.PostRepository;
import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.event.PostCreated;
import dooya.see.domain.post.event.PostDeleted;
import dooya.see.domain.post.event.PostUpdated;
import dooya.see.domain.shared.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class PostEventProcessor {
    private final PostSearchIndexer postSearchIndexer;
    private final PostRepository postRepository;

    void process(DomainEvent event) {
        if (event instanceof PostCreated created) {
            handlePostCreated(created);
        } else if (event instanceof PostUpdated updated) {
            handlePostUpdated(updated);
        } else if (event instanceof PostDeleted deleted) {
            handlePostDeleted(deleted);
        } else {
            log.debug("처리할 필요 없는 이벤트 타입: {}", event.getClass().getName());
        }
    }

    void process(PostEventMessage message) {
        if (message == null) {
            return;
        }

        try {
            switch (message.type()) {
                case CREATED, UPDATED -> indexLatestSnapshot(message.postId());
                case DELETED -> deleteIndex(message.postId());
                default -> log.debug("처리할 필요 없는 메시지 타입: {}", message.type());
            }
        } catch (Exception ex) {
            log.error("Kafka 이벤트 메시지 처리 실패: {}", message, ex);
        }
    }

    private void indexLatestSnapshot(Long postId) {
        postRepository.findById(postId)
                .ifPresentOrElse(this::safeIndex,
                        () -> log.warn("색인 대상 게시글을 찾을 수 없습니다: postId={}", postId));
    }

    private void deleteIndex(Long postId) {
        try {
            log.debug("Kafka 메시지 처리 - 게시글 삭제 색인 제거: {}", postId);
            postSearchIndexer.delete(postId);
        } catch (Exception e) {
            log.error("게시글 삭제 색인 처리 실패: postId={}", postId, e);
        }
    }

    private void safeIndex(Post post) {
        try {
            log.debug("Kafka 메시지 처리 - 게시글 색인 갱신: {}", post.getId());
            postSearchIndexer.index(post);
        } catch (Exception e) {
            log.error("게시글 색인 처리 실패: postId={}", post.getId(), e);
        }
    }

    private void handlePostCreated(PostCreated event) {
        try {
            log.debug("도메인 이벤트 처리 - 게시글 생성 색인: {}", event.postId());
            postSearchIndexer.index(event.post());
        } catch (Exception e) {
            log.error("게시글 생성 색인 처리 실패: {}", event, e);
        }
    }

    private void handlePostUpdated(PostUpdated event) {
        try {
            log.debug("도메인 이벤트 처리 - 게시글 업데이트 색인: {}", event.postId());
            postSearchIndexer.index(event.post());
        } catch (Exception e) {
            log.error("게시글 업데이트 색인 처리 실패: {}", event, e);
        }
    }

    private void handlePostDeleted(PostDeleted event) {
        try {
            log.debug("도메인 이벤트 처리 - 게시글 삭제 색인 제거: {}", event.postId());
            postSearchIndexer.delete(event.postId());
        } catch (Exception e) {
            log.error("게시글 삭제 색인 처리 실패: {}", event, e);
        }
    }
}
