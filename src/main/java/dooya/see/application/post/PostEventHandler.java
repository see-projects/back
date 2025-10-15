package dooya.see.application.post;

import dooya.see.application.post.required.PostSearchIndexer;
import dooya.see.domain.post.event.PostCreated;
import dooya.see.domain.post.event.PostDeleted;
import dooya.see.domain.post.event.PostUpdated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventHandler {
    private final PostSearchIndexer postSearchIndexer;

    @EventListener
    public void handlePostCreated(PostCreated event) {
        log.info("[ES] 게시글 생성 색인: {}", event.postId());
        postSearchIndexer.index(event.post());
    }

    @EventListener
    public void handlePostUpdated(PostUpdated event) {
        log.info("[ES] 게시글 업데이트 색인: {}", event.postId());
        postSearchIndexer.index(event.post());
    }

    @EventListener
    public void handlePostDeleted(PostDeleted event) {
        log.info("[ES] 게시글 색인 삭제: {}", event.postId());
        postSearchIndexer.delete(event.postId());
    }
}
