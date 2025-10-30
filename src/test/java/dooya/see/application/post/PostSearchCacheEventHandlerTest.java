package dooya.see.application.post;

import dooya.see.application.post.PostSearchCacheEventHandler.PostSearchCacheEvictCommand;
import dooya.see.application.post.required.PostSearchCacheRepository;
import dooya.see.domain.post.event.PostCreated;
import dooya.see.domain.post.event.PostDeleted;
import dooya.see.domain.post.event.PostHidden;
import dooya.see.domain.post.event.PostPublished;
import dooya.see.domain.post.event.PostUpdated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostSearchCacheEventHandlerTest {
    private static final Long POST_ID = 42L;

    @Mock
    private PostSearchCacheRepository cacheRepository;

    private PostSearchCacheEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PostSearchCacheEventHandler(cacheRepository);
    }

    @Test
    void 게시글_생성_이벤트_수신시_캐시_무효화() {
        handler.handlePostCreated(new PostCreated(POST_ID, null, null, false, null));
        verify(cacheRepository).evictAll();
    }

    @Test
    void 게시글_업데이트_이벤트_수신시_캐시_무효화() {
        handler.handlePostUpdated(new PostUpdated(POST_ID, null, false, false, false, false, null));
        verify(cacheRepository).evictAll();
    }

    @Test
    void 게시글_발행_이벤트_수신시_캐시_무효화() {
        handler.handlePostPublished(new PostPublished(POST_ID, null));
        verify(cacheRepository).evictAll();
    }

    @Test
    void 게시글_숨김_이벤트_수신시_캐시_무효화() {
        handler.handlePostHidden(new PostHidden(POST_ID, null, null));
        verify(cacheRepository).evictAll();
    }

    @Test
    void 게시글_삭제_이벤트_수신시_캐시_무효화() {
        handler.handlePostDeleted(new PostDeleted(POST_ID, null, null));
        verify(cacheRepository).evictAll();
    }

    @Test
    void 명령으로_캐시_무효화() {
        handler.handleCacheEvictRequest(new PostSearchCacheEvictCommand("test", POST_ID));
        verify(cacheRepository).evictAll();
    }
}
