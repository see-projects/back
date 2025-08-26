package dooya.see.application.post;

import dooya.see.application.post.required.PostStatsRepository;
import dooya.see.domain.post.PostStats;
import dooya.see.domain.post.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class PostStatsEventHandler {
    private final PostStatsRepository postStatsRepository;

    @EventListener
    public void handlePostCreated(PostCreated event) {
        log.info("PostCreated 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());
        PostStats stats = PostStats.create(event.postId());
        postStatsRepository.save(stats);
    }

    @EventListener
    public void handlePostViewed(PostViewed event) {
        if (event.postId() != null) {
            log.info("PostViewed 이벤트 처리: postId={}, viewerId={}", event.postId(), event.memberId());
            postStatsRepository.findByPostId(event.postId())
                .ifPresent(PostStats::incrementViewCount);
        }
    }

    @EventListener
    public void handlePostLiked(PostLiked event) {
        if (event.postId() != null) {
            log.info("PostLiked 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());
            postStatsRepository.findByPostId(event.postId())
                .ifPresent(PostStats::incrementLikeCount);
        }
    }

    @EventListener
    public void handlePostUnliked(PostUnliked event) {
        if (event.postId() != null) {
            log.info("PostUnliked 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());
            postStatsRepository.findByPostId(event.postId())
                .ifPresent(PostStats::decrementLikeCount);
        }
    }

    @EventListener
    public void handlePostUpdated(PostUpdated event) {
        log.info("PostUpdated 이벤트 처리: postId={}, memberId={}, titleChanged={}, bodyChanged={}, categoryChanged={}",
            event.postId(), event.memberId(), event.titleChanged(), event.bodyChanged(), event.categoryChanged());
        // 게시글 수정 시 특별한 통계 처리는 없지만 로그는 남김
    }

    @EventListener
    public void handlePostPublished(PostPublished event) {
        log.info("PostPublished 이벤트 처리: postId={}, memberId={}", event.postId(), event.memberId());
        // 게시글 발행 시 특별한 통계 처리는 없지만 로그는 남김
    }

    @EventListener
    public void handlePostHidden(PostHidden event) {
        log.info("PostHidden 이벤트 처리: postId={}, memberId={}, previousStatus={}",
            event.postId(), event.memberId(), event.previousStatus());
        // 게시글 숨김 시 특별한 통계 처리는 없지만 로그는 남김
    }

    @EventListener
    public void handlePostDeleted(PostDeleted event) {
        log.info("PostDeleted 이벤트 처리: postId={}, memberId={}, previousStatus={}",
            event.postId(), event.memberId(), event.previousStatus());
        // 게시글 삭제 시 통계 데이터도 삭제할 수 있지만, 지금은 로그만 남김
        // 실제로는 비즈니스 요구사항에 따라 통계 데이터 보존/삭제 결정
    }
}