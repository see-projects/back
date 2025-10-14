package dooya.see.application.post;

import dooya.see.application.post.provided.PostStatsManager;
import dooya.see.domain.post.event.*;
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
public class PostStatsEventHandler {
    private final PostStatsManager postStatsManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCreated(PostCreated event) {
        log.info("PostCreated 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());

        try {
            postStatsManager.initializePostStats(event.postId());
        } catch (Exception e) {
            log.error("게시물 통계 초기화 실패: postId={}", event.postId(), e);
        }
    }

    @Async
    @EventListener
    public void handlePostViewed(PostViewed event) {
        if (event.postId() == null) return;

        log.info("PostViewed 이벤트 처리: postId={}, viewerId={}", event.postId(), event.memberId());

        try {
            postStatsManager.incrementViewCount(event.postId());
        } catch (Exception e) {
            log.error("조회수 증가 실패: postId={}", event.postId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLiked(PostLiked event) {
        if (event.postId() == null) return;

        log.info("PostLiked 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());

        try {
            postStatsManager.incrementLikeCount(event.postId());
        } catch (Exception e) {
            log.error("좋아요 수 증가 실패: postId={}", event.postId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostUnliked(PostUnliked event) {
        if (event.postId() == null) return;

        log.info("PostUnliked 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());

        try {
            postStatsManager.decrementLikeCount(event.postId());
        } catch (Exception e) {
            log.error("좋아요 수 감소 실패: postId={}", event.postId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreated event) {
        if (event.postId() == null) return;

        log.info("CommentCreated 이벤트 처리: postId={}, commentId={}, isReply={}",
                event.postId(), event.commentId(), event.isReply());

        try {
            postStatsManager.incrementCommentCount(event.postId());
        } catch (Exception e) {
            log.error("댓글 수 증가 실패: postId={}", event.postId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(CommentDeleted event) {
        if (event.postId() == null) return;

        log.info("CommentDeleted 이벤트 처리: postId={}, commentId={}, isReply={}",
                event.postId(), event.commentId(), event.isReply());

        try {
            postStatsManager.decrementCommentCount(event.postId());
        } catch (Exception e) {
            log.error("댓글 수 감소 실패: postId={}", event.postId(), e);
        }
    }

    // 다른 이벤트들은 로깅만 수행
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostUpdated(PostUpdated event) {
        log.info("PostUpdated 이벤트 처리: postId={}, memberId={}, titleChanged={}, bodyChanged={}, categoryChanged={}",
                event.postId(), event.memberId(), event.titleChanged(), event.bodyChanged(), event.categoryChanged());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostPublished(PostPublished event) {
        log.info("PostPublished 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostHidden(PostHidden event) {
        log.info("PostHidden 이벤트 처리: postId={}, memberId={}, previousStatus={}",
                event.postId(), event.memberId(), event.previousStatus());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostDeleted(PostDeleted event) {
        log.info("PostDeleted 이벤트 처리: postId={}, memberId={}, previousStatus={}",
                event.postId(), event.memberId(), event.previousStatus());
    }
}
