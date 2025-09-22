package dooya.see.application.post;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.provided.PostStatsManager;
import dooya.see.application.post.required.PostStatsRepository;
import dooya.see.domain.post.PostStats;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.event.*;
import org.junit.jupiter.api.Nested;
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
record PostStatsEventHandlerTest(PostStatsEventHandler postStatsEventHandler, PostStatsManager postStatsManager, PostStatsRepository postStatsRepository) {
    private static final Long POST_ID = 100L;
    private static final Long MEMBER_ID = 1L;
    private static final Long ANOTHER_MEMBER_ID = 2L;
    private static final Long NON_EXISTENT_POST_ID = 999L;

    @Nested
    class 게시물_생성_이벤트 {
        @Test
        void PostCreated_이벤트_처리_시_통계가_생성된다() {
            PostCreated event = new PostCreated(POST_ID, MEMBER_ID, TECH, false);

            postStatsEventHandler.handlePostCreated(event);

            assertThatStatsCreated(POST_ID);
        }

        private void assertThatStatsCreated(Long postId) {
            PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
            assertThat(stats.getPostId()).isEqualTo(postId);
            assertThat(stats.getViewCount()).isEqualTo(0);
            assertThat(stats.getLikeCount()).isEqualTo(0);
            assertThat(stats.getCommentCount()).isEqualTo(0);
        }
    }

    @Nested
    class 게시물_조회_이벤트 {
        @Test
        void PostViewed_이벤트_처리_시_조회수가_증가한다() {
            postStatsManager.initializePostStats(POST_ID);
            PostViewed event = new PostViewed(POST_ID, MEMBER_ID);

            postStatsEventHandler.handlePostViewed(event);

            assertThatViewCountIncremented(POST_ID, 1);
        }

        @Test
        void 익명_사용자의_PostViewed_이벤트도_정상_처리된다() {
            postStatsManager.initializePostStats(POST_ID);
            PostViewed event = new PostViewed(POST_ID, null);

            postStatsEventHandler.handlePostViewed(event);

            assertThatViewCountIncremented(POST_ID, 1);
        }

        @Test
        void 존재하지_않는_게시물의_PostViewed_이벤트는_조용히_무시된다() {
            PostViewed event = new PostViewed(NON_EXISTENT_POST_ID, MEMBER_ID);

            assertThatCode(() -> postStatsEventHandler.handlePostViewed(event))
                    .doesNotThrowAnyException();

            assertThat(postStatsRepository.findByPostId(NON_EXISTENT_POST_ID)).isEmpty();
        }

        private void assertThatViewCountIncremented(Long postId, int expectedCount) {
            PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
            assertThat(stats.getViewCount()).isEqualTo(expectedCount);
        }
    }

    @Nested
    class 게시물_좋아요_이벤트 {
        @Test
        void PostLiked_이벤트_처리_시_좋아요_수가_증가한다() {
            postStatsManager.initializePostStats(POST_ID);
            PostLiked event = new PostLiked(POST_ID, MEMBER_ID);

            postStatsEventHandler.handlePostLiked(event);

            assertThatLikeCountIncremented(POST_ID, 1);
        }

        @Test
        void PostUnliked_이벤트_처리_시_좋아요_수가_감소한다() {
            postStatsManager.initializePostStats(POST_ID);
            postStatsManager.incrementLikeCount(POST_ID);
            PostUnliked event = new PostUnliked(POST_ID, MEMBER_ID);

            postStatsEventHandler.handlePostUnliked(event);

            assertThatLikeCountDecremented(POST_ID, 0);
        }

        @Test
        void 좋아요_수가_0일_때_PostUnliked_이벤트_처리해도_음수가_되지_않는다() {
            postStatsManager.initializePostStats(POST_ID);
            PostUnliked event = new PostUnliked(POST_ID, MEMBER_ID);

            postStatsEventHandler.handlePostUnliked(event);

            assertThatLikeCountRemainsSafe(POST_ID);
        }

        @Test
        void 존재하지_않는_게시물의_PostLiked_이벤트는_조용히_무시된다() {
            PostLiked event = new PostLiked(NON_EXISTENT_POST_ID, MEMBER_ID);

            assertThatCode(() -> postStatsEventHandler.handlePostLiked(event))
                    .doesNotThrowAnyException();

            assertThat(postStatsRepository.findByPostId(NON_EXISTENT_POST_ID)).isEmpty();
        }

        @Test
        void 존재하지_않는_게시물의_PostUnliked_이벤트는_조용히_무시된다() {
            PostUnliked event = new PostUnliked(NON_EXISTENT_POST_ID, MEMBER_ID);

            assertThatCode(() -> postStatsEventHandler.handlePostUnliked(event))
                    .doesNotThrowAnyException();

            assertThat(postStatsRepository.findByPostId(NON_EXISTENT_POST_ID)).isEmpty();
        }

        private void assertThatLikeCountIncremented(Long postId, int expectedCount) {
            PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
            assertThat(stats.getLikeCount()).isEqualTo(expectedCount);
            assertThat(stats.getViewCount()).isEqualTo(0);
            assertThat(stats.getCommentCount()).isEqualTo(0);
        }

        private void assertThatLikeCountDecremented(Long postId, int expectedCount) {
            PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
            assertThat(stats.getLikeCount()).isEqualTo(expectedCount);
        }

        private void assertThatLikeCountRemainsSafe(Long postId) {
            PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
            assertThat(stats.getLikeCount()).isEqualTo(0);
        }
    }

    @Nested
    class 복합_이벤트_처리 {
        @Test
        void 여러_이벤트를_순차적으로_처리할_수_있다() {
            postStatsManager.initializePostStats(POST_ID);

            processMultipleEvents(POST_ID);

            assertThatMultipleEventsProcessed(POST_ID);
        }

        private void processMultipleEvents(Long postId) {
            PostViewed viewEvent1 = new PostViewed(postId, MEMBER_ID);
            PostViewed viewEvent2 = new PostViewed(postId, ANOTHER_MEMBER_ID);
            PostLiked likeEvent1 = new PostLiked(postId, MEMBER_ID);
            PostLiked likeEvent2 = new PostLiked(postId, ANOTHER_MEMBER_ID);
            PostUnliked unlikeEvent = new PostUnliked(postId, MEMBER_ID);

            postStatsEventHandler.handlePostViewed(viewEvent1);
            postStatsEventHandler.handlePostViewed(viewEvent2);
            postStatsEventHandler.handlePostLiked(likeEvent1);
            postStatsEventHandler.handlePostLiked(likeEvent2);
            postStatsEventHandler.handlePostUnliked(unlikeEvent);
        }

        private void assertThatMultipleEventsProcessed(Long postId) {
            PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
            assertThat(stats.getViewCount()).isEqualTo(2);
            assertThat(stats.getLikeCount()).isEqualTo(1);
            assertThat(stats.getCommentCount()).isEqualTo(0);
        }
    }

    @Nested
    class 기타_이벤트_처리 {
        @Test
        void PostUpdated_이벤트는_조용히_처리된다() {
            PostUpdated event = new PostUpdated(POST_ID, MEMBER_ID, true, false, true);

            assertThatCode(() -> postStatsEventHandler.handlePostUpdated(event))
                    .doesNotThrowAnyException();
        }

        @Test
        void PostPublished_이벤트는_조용히_처리된다() {
            PostPublished event = new PostPublished(POST_ID, MEMBER_ID);

            assertThatCode(() -> postStatsEventHandler.handlePostPublished(event))
                    .doesNotThrowAnyException();
        }

        @Test
        void PostHidden_이벤트는_조용히_처리된다() {
            PostHidden event = new PostHidden(POST_ID, MEMBER_ID, PostStatus.PUBLISHED);

            assertThatCode(() -> postStatsEventHandler.handlePostHidden(event))
                    .doesNotThrowAnyException();
        }

        @Test
        void PostDeleted_이벤트는_조용히_처리된다() {
            PostDeleted event = new PostDeleted(POST_ID, MEMBER_ID, PostStatus.PUBLISHED);

            assertThatCode(() -> postStatsEventHandler.handlePostDeleted(event))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class 예외_상황_처리 {
        @Test
        void postId가_null인_이벤트들은_조용히_무시된다() {
            PostViewed viewEvent = new PostViewed(null, MEMBER_ID);
            PostLiked likeEvent = new PostLiked(null, MEMBER_ID);
            PostUnliked unlikeEvent = new PostUnliked(null, MEMBER_ID);

            assertThatCode(() -> processNullPostIdEvents(viewEvent, likeEvent, unlikeEvent))
                    .doesNotThrowAnyException();
        }

        private void processNullPostIdEvents(PostViewed viewEvent, PostLiked likeEvent, PostUnliked unlikeEvent) {
            postStatsEventHandler.handlePostViewed(viewEvent);
            postStatsEventHandler.handlePostLiked(likeEvent);
            postStatsEventHandler.handlePostUnliked(unlikeEvent);
        }
    }
}