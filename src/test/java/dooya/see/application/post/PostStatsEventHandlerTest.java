package dooya.see.application.post;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.PostStatsRepository;
import dooya.see.domain.post.PostStats;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.event.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostCategory.TECH;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostStatsEventHandlerTest(PostStatsEventHandler postStatsEventHandler, PostStatsRepository postStatsRepository) {
    @DisplayName("PostCreated 이벤트 처리 시 PostStats가 생성된다")
    @Test
    void handlePostCreated() {
        PostCreated event = new PostCreated(100L, 1L, TECH, false);

        postStatsEventHandler.handlePostCreated(event);

        PostStats stats = postStatsRepository.findByPostId(100L).orElseThrow();
        assertThat(stats.getPostId()).isEqualTo(100L);
        assertThat(stats.getViewCount()).isEqualTo(0);
        assertThat(stats.getLikeCount()).isEqualTo(0);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    @DisplayName("PostViewed 이벤트 처리 시 조회수가 증가한다")
    @Test
    void handlePostViewed() {
        Long postId = 200L;
        createPostStats(postId);
        PostViewed event = new PostViewed(postId, 2L);

        postStatsEventHandler.handlePostViewed(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(1);
        assertThat(stats.getLikeCount()).isEqualTo(0);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    @DisplayName("익명 사용자의 PostViewed 이벤트도 정상 처리된다")
    @Test
    void handlePostViewedByAnonymousUser() {
        Long postId = 300L;
        createPostStats(postId);
        PostViewed event = new PostViewed(postId, null);

        postStatsEventHandler.handlePostViewed(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(1);
    }

    @DisplayName("PostLiked 이벤트 처리 시 좋아요 수가 증가한다")
    @Test
    void handlePostLiked() {
        Long postId = 400L;
        createPostStats(postId);
        PostLiked event = new PostLiked(postId, 3L);

        postStatsEventHandler.handlePostLiked(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(0);
        assertThat(stats.getLikeCount()).isEqualTo(1);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    @DisplayName("PostUnliked 이벤트 처리 시 좋아요 수가 감소한다")
    @Test
    void handlePostUnliked() {
        Long postId = 500L;
        PostStats stats = createPostStats(postId);
        stats.incrementLikeCount();
        PostUnliked event = new PostUnliked(postId, 4L);

        postStatsEventHandler.handlePostUnliked(event);

        PostStats updatedStats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(updatedStats.getLikeCount()).isEqualTo(0);
    }

    @DisplayName("좋아요 수가 0일 때 PostUnliked 이벤트 처리해도 음수가 되지 않는다")
    @Test
    void handlePostUnlikedWhenLikeCountIsZero() {
        Long postId = 600L;
        createPostStats(postId);
        PostUnliked event = new PostUnliked(postId, 5L);

        postStatsEventHandler.handlePostUnliked(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getLikeCount()).isEqualTo(0);
    }

    @DisplayName("여러 이벤트를 순차적으로 처리할 수 있다")
    @Test
    void handleMultipleEvents() {
        Long postId = 700L;
        createPostStats(postId);

        PostViewed viewEvent1 = new PostViewed(postId, 1L);
        PostViewed viewEvent2 = new PostViewed(postId, 2L);
        PostLiked likeEvent1 = new PostLiked(postId, 3L);
        PostLiked likeEvent2 = new PostLiked(postId, 4L);
        PostUnliked unlikeEvent = new PostUnliked(postId, 3L);

        postStatsEventHandler.handlePostViewed(viewEvent1);
        postStatsEventHandler.handlePostViewed(viewEvent2);
        postStatsEventHandler.handlePostLiked(likeEvent1);
        postStatsEventHandler.handlePostLiked(likeEvent2);
        postStatsEventHandler.handlePostUnliked(unlikeEvent);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(2);
        assertThat(stats.getLikeCount()).isEqualTo(1);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    @DisplayName("존재하지 않는 게시글의 PostViewed 이벤트는 조용히 무시된다")
    @Test
    void handlePostViewedForNonExistentPost() {
        PostViewed event = new PostViewed(999L, 1L);

        assertThatCode(() -> postStatsEventHandler.handlePostViewed(event))
                .doesNotThrowAnyException();

        assertThat(postStatsRepository.findByPostId(999L)).isEmpty();
    }

    @DisplayName("존재하지 않는 게시글의 PostLiked 이벤트는 조용히 무시된다")
    @Test
    void handlePostLikedForNonExistentPost() {
        PostLiked event = new PostLiked(888L, 1L);

        assertThatCode(() -> postStatsEventHandler.handlePostLiked(event))
                .doesNotThrowAnyException();

        assertThat(postStatsRepository.findByPostId(888L)).isEmpty();
    }

    @DisplayName("PostUpdated 이벤트는 로그만 남기고 특별한 처리를 하지 않는다")
    @Test
    void handlePostUpdated() {
        PostUpdated event = new PostUpdated(100L, 1L, true, false, true);

        assertThatCode(() -> postStatsEventHandler.handlePostUpdated(event))
                .doesNotThrowAnyException();
    }

    @DisplayName("PostPublished 이벤트는 로그만 남기고 특별한 처리를 하지 않는다")
    @Test
    void handlePostPublished() {
        PostPublished event = new PostPublished(100L, 1L);

        assertThatCode(() -> postStatsEventHandler.handlePostPublished(event))
                .doesNotThrowAnyException();
    }

    @DisplayName("PostHidden 이벤트는 로그만 남기고 특별한 처리를 하지 않는다")
    @Test
    void handlePostHidden() {
        PostHidden event = new PostHidden(100L, 1L, PostStatus.PUBLISHED);

        assertThatCode(() -> postStatsEventHandler.handlePostHidden(event))
                .doesNotThrowAnyException();
    }

    @DisplayName("PostDeleted 이벤트는 로그만 남기고 특별한 처리를 하지 않는다")
    @Test
    void handlePostDeleted() {
        PostDeleted event = new PostDeleted(100L, 1L, PostStatus.PUBLISHED);

        assertThatCode(() -> postStatsEventHandler.handlePostDeleted(event))
                .doesNotThrowAnyException();
    }

    @DisplayName("postId가 null인 이벤트들은 조용히 무시된다")
    @Test
    void handleEventsWithNullPostId() {
        PostViewed viewEvent = new PostViewed(null, 1L);
        PostLiked likeEvent = new PostLiked(null, 1L);
        PostUnliked unlikeEvent = new PostUnliked(null, 1L);

        assertThatCode(() -> {
            postStatsEventHandler.handlePostViewed(viewEvent);
            postStatsEventHandler.handlePostLiked(likeEvent);
            postStatsEventHandler.handlePostUnliked(unlikeEvent);
        }).doesNotThrowAnyException();
    }

    private PostStats createPostStats(Long postId) {
        PostStats stats = PostStats.create(postId);
        return postStatsRepository.save(stats);
    }
}
