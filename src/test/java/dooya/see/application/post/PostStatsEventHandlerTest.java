package dooya.see.application.post;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.PostStatsRepository;
import dooya.see.domain.post.PostStats;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.event.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostCategory.TECH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostStatsEventHandlerTest(PostStatsEventHandler postStatsEventHandler, PostStatsRepository postStatsRepository) {
    @Test
    void PostCreated_이벤트_처리_시_PostStats가_생성된다() {
        PostCreated event = new PostCreated(100L, 1L, TECH, false);

        postStatsEventHandler.handlePostCreated(event);

        PostStats stats = postStatsRepository.findByPostId(100L).orElseThrow();
        assertThat(stats.getPostId()).isEqualTo(100L);
        assertThat(stats.getViewCount()).isEqualTo(0);
        assertThat(stats.getLikeCount()).isEqualTo(0);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    @Test
    void PostViewed_이벤트_처리_시_조회수가_증가한다() {
        Long postId = 200L;
        createPostStats(postId);
        PostViewed event = new PostViewed(postId, 2L);

        postStatsEventHandler.handlePostViewed(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(1);
        assertThat(stats.getLikeCount()).isEqualTo(0);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    @Test
    void 익명_사용자의_PostViewed_이벤트도_정상_처리된다() {
        Long postId = 300L;
        createPostStats(postId);
        PostViewed event = new PostViewed(postId, null);

        postStatsEventHandler.handlePostViewed(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(1);
    }

    @Test
    void PostLiked_이벤트_처리_시_좋아요_수가_증가한다() {
        Long postId = 400L;
        createPostStats(postId);
        PostLiked event = new PostLiked(postId, 3L);

        postStatsEventHandler.handlePostLiked(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(0);
        assertThat(stats.getLikeCount()).isEqualTo(1);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    @Test
    void PostUnliked_이벤트_처리_시_좋아요_수가_감소한다() {
        Long postId = 500L;
        PostStats stats = createPostStats(postId);
        stats.incrementLikeCount();
        PostUnliked event = new PostUnliked(postId, 4L);

        postStatsEventHandler.handlePostUnliked(event);

        PostStats updatedStats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(updatedStats.getLikeCount()).isEqualTo(0);
    }

    @Test
    void 좋아요_수가_0일_때_PostUnliked_이벤트_처리해도_음수가_되지_않는다() {
        Long postId = 600L;
        createPostStats(postId);
        PostUnliked event = new PostUnliked(postId, 5L);

        postStatsEventHandler.handlePostUnliked(event);

        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getLikeCount()).isEqualTo(0);
    }

    @Test
    void 여러_이벤트를_순차적으로_처리할_수_있다() {
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

    @Test
    void 존재하지_않는_게시글의_PostViewed_이벤트는_조용히_무시된다() {
        PostViewed event = new PostViewed(999L, 1L);

        assertThatCode(() -> postStatsEventHandler.handlePostViewed(event))
                .doesNotThrowAnyException();

        assertThat(postStatsRepository.findByPostId(999L)).isEmpty();
    }

    @Test
    void 존재하지_않는_게시글의_PostLiked_이벤트는_조용히_무시된다() {
        PostLiked event = new PostLiked(888L, 1L);

        assertThatCode(() -> postStatsEventHandler.handlePostLiked(event))
                .doesNotThrowAnyException();

        assertThat(postStatsRepository.findByPostId(888L)).isEmpty();
    }

    @Test
    void PostUpdated_이벤트는_로그만_남기고_특별한_처리를_하지_않는다() {
        PostUpdated event = new PostUpdated(100L, 1L, true, false, true);

        assertThatCode(() -> postStatsEventHandler.handlePostUpdated(event))
                .doesNotThrowAnyException();
    }

    @Test
    void PostPublished_이벤트는_로그만_남기고_특별한_처리를_하지_않는다() {
        PostPublished event = new PostPublished(100L, 1L);

        assertThatCode(() -> postStatsEventHandler.handlePostPublished(event))
                .doesNotThrowAnyException();
    }

    @Test
    void PostHidden_이벤트는_로그만_남기고_특별한_처리를_하지_않는다() {
        PostHidden event = new PostHidden(100L, 1L, PostStatus.PUBLISHED);

        assertThatCode(() -> postStatsEventHandler.handlePostHidden(event))
                .doesNotThrowAnyException();
    }

    @Test
    void PostDeleted_이벤트는_로그만_남기고_특별한_처리를_하지_않는다() {
        PostDeleted event = new PostDeleted(100L, 1L, PostStatus.PUBLISHED);

        assertThatCode(() -> postStatsEventHandler.handlePostDeleted(event))
                .doesNotThrowAnyException();
    }

    @Test
    void postId가_null인_이벤트들은_조용히_무시된다() {
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
