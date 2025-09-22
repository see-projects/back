package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.PostStatsRepository;
import dooya.see.domain.post.PostStats;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostStatsManagerTest(PostStatsManager postStatsManager, PostStatsRepository postStatsRepository) {
    private static final Long POST_ID = 100L;
    private static final Long ANOTHER_POST_ID = 200L;
    private static final Long NON_EXISTENT_POST_ID = 999L;

    @Nested
    class 통계_초기화 {
        @Test
        void 게시물_통계를_초기화할_수_있다() {
            postStatsManager.initializePostStats(POST_ID);

            assertThatStatsInitialized(POST_ID);
        }

        @Test
        void 이미_존재하는_게시물_통계는_중복_생성되지_않는다() {
            postStatsManager.initializePostStats(POST_ID);
            long initialCount = postStatsRepository.count();

            postStatsManager.initializePostStats(POST_ID);

            assertThatNoDuplicateStatsCreated(initialCount);
            assertThatStatsExistsFor(POST_ID);
        }
    }

    @Nested
    class 조회수_관리 {
        @Test
        void 조회수를_증가시킬_수_있다() {
            initializeTestStats(POST_ID);

            postStatsManager.incrementViewCount(POST_ID);

            assertThatViewCountIs(POST_ID, 1);
        }

        @Test
        void 조회수를_여러_번_증가시킬_수_있다() {
            initializeTestStats(POST_ID);

            incrementViewCountTimes(POST_ID, 3);

            assertThatViewCountIs(POST_ID, 3);
        }

        @Test
        void 존재하지_않는_게시물의_조회수_증가는_조용히_무시된다() {
            assertThatOperationDoesNotFail(() -> postStatsManager.incrementViewCount(NON_EXISTENT_POST_ID));
            assertThatStatsDoesNotExistFor(NON_EXISTENT_POST_ID);
        }
    }

    @Nested
    class 좋아요_관리 {
        @Test
        void 좋아요_수를_증가시킬_수_있다() {
            initializeTestStats(POST_ID);

            postStatsManager.incrementLikeCount(POST_ID);

            assertThatLikeCountIs(POST_ID, 1);
            assertThatOnlyLikeCountChanged(POST_ID);
        }

        @Test
        void 좋아요_수를_감소시킬_수_있다() {
            initializeTestStats(POST_ID);
            incrementLikeCountTimes(POST_ID, 2);

            postStatsManager.decrementLikeCount(POST_ID);

            assertThatLikeCountIs(POST_ID, 1);
        }

        @Test
        void 좋아요_수가_0일_때_감소시켜도_음수가_되지_않는다() {
            initializeTestStats(POST_ID);

            postStatsManager.decrementLikeCount(POST_ID);

            assertThatLikeCountRemainsSafe(POST_ID);
        }

        @Test
        void 존재하지_않는_게시물의_좋아요_증가는_조용히_무시된다() {
            assertThatOperationDoesNotFail(() -> postStatsManager.incrementLikeCount(NON_EXISTENT_POST_ID));
            assertThatStatsDoesNotExistFor(NON_EXISTENT_POST_ID);
        }

        @Test
        void 존재하지_않는_게시물의_좋아요_감소는_조용히_무시된다() {
            assertThatOperationDoesNotFail(() -> postStatsManager.decrementLikeCount(NON_EXISTENT_POST_ID));
            assertThatStatsDoesNotExistFor(NON_EXISTENT_POST_ID);
        }
    }

    @Nested
    class 댓글_수_관리 {
        @Test
        void 댓글_수를_증가시킬_수_있다() {
            initializeTestStats(POST_ID);

            postStatsManager.incrementCommentCount(POST_ID);

            assertThatCommentCountIs(POST_ID, 1);
            assertThatOnlyCommentCountChanged(POST_ID);
        }

        @Test
        void 댓글_수를_감소시킬_수_있다() {
            initializeTestStats(POST_ID);
            incrementCommentCountTimes(POST_ID, 2);

            postStatsManager.decrementCommentCount(POST_ID);

            assertThatCommentCountIs(POST_ID, 1);
        }

        @Test
        void 댓글_수가_0일_때_감소시켜도_음수가_되지_않는다() {
            initializeTestStats(POST_ID);

            postStatsManager.decrementCommentCount(POST_ID);

            assertThatCommentCountRemainsSafe(POST_ID);
        }

        @Test
        void 존재하지_않는_게시물의_댓글_수_증가는_조용히_무시된다() {
            assertThatOperationDoesNotFail(() -> postStatsManager.incrementCommentCount(NON_EXISTENT_POST_ID));
            assertThatStatsDoesNotExistFor(NON_EXISTENT_POST_ID);
        }

        @Test
        void 존재하지_않는_게시물의_댓글_수_감소는_조용히_무시된다() {
            assertThatOperationDoesNotFail(() -> postStatsManager.decrementCommentCount(NON_EXISTENT_POST_ID));
            assertThatStatsDoesNotExistFor(NON_EXISTENT_POST_ID);
        }
    }

    @Nested
    class 통계_조회 {
        @Test
        void 게시물_통계를_조회할_수_있다() {
            setupCompleteStats(POST_ID);

            Optional<PostStats> result = postStatsManager.getPostStats(POST_ID);

            assertThatStatsFound(result);
            assertThatCompleteStatsCorrect(result.get());
        }

        @Test
        void 존재하지_않는_게시물_통계_조회_시_Empty를_반환한다() {
            Optional<PostStats> result = postStatsManager.getPostStats(NON_EXISTENT_POST_ID);

            assertThatStatsNotFound(result);
        }

        @Test
        void getPostStatsOrThrow로_게시물_통계를_조회할_수_있다() {
            initializeTestStats(POST_ID);
            postStatsManager.incrementViewCount(POST_ID);

            PostStats stats = postStatsManager.getPostStatsOrThrow(POST_ID);

            assertThatStatsFoundWithPostId(stats, POST_ID);
            assertThatViewCountIs(stats, 1);
        }

        @Test
        void getPostStatsOrThrow로_존재하지_않는_게시물_조회_시_예외가_발생한다() {
            assertThatThrownBy(() -> postStatsManager.getPostStatsOrThrow(NON_EXISTENT_POST_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("PostStats not found for postId: " + NON_EXISTENT_POST_ID);
        }
    }

    @Nested
    class 복합_시나리오 {
        @Test
        void 모든_통계를_종합적으로_관리할_수_있다() {
            initializeTestStats(POST_ID);

            performComprehensiveStatsOperations(POST_ID);

            assertThatComprehensiveStatsCorrect(POST_ID);
        }

        @Test
        void 여러_게시물의_통계를_독립적으로_관리할_수_있다() {
            setupIndependentPostStats();

            assertThatIndependentStatsCorrect();
        }

        @Test
        void 도메인_로직을_활용한_통계_분석이_가능하다() {
            initializeTestStats(POST_ID);
            setupPopularPostStats(POST_ID);

            PostStats stats = postStatsManager.getPostStatsOrThrow(POST_ID);

            assertThatPostIsPopular(stats);
            assertThatPostHasEngagement(stats);
            assertThatStatsAreValid(stats);
        }
    }

    // 헬퍼 메서드들
    private void initializeTestStats(Long postId) {
        postStatsManager.initializePostStats(postId);
    }

    private void incrementViewCountTimes(Long postId, int times) {
        for (int i = 0; i < times; i++)
            postStatsManager.incrementViewCount(postId);
    }

    private void incrementLikeCountTimes(Long postId, int times) {
        for (int i = 0; i < times; i++)
            postStatsManager.incrementLikeCount(postId);
    }

    private void incrementCommentCountTimes(Long postId, int times) {
        for (int i = 0; i < times; i++)
            postStatsManager.incrementCommentCount(postId);
    }

    private void setupCompleteStats(Long postId) {
        initializeTestStats(postId);
        postStatsManager.incrementViewCount(postId);
        postStatsManager.incrementLikeCount(postId);
        postStatsManager.incrementCommentCount(postId);
    }

    private void performComprehensiveStatsOperations(Long postId) {
        incrementViewCountTimes(postId, 3);
        incrementLikeCountTimes(postId, 2);
        postStatsManager.decrementLikeCount(postId);
        postStatsManager.incrementCommentCount(postId);
    }

    private void setupIndependentPostStats() {
        initializeTestStats(POST_ID);
        initializeTestStats(ANOTHER_POST_ID);

        postStatsManager.incrementViewCount(POST_ID);
        postStatsManager.incrementLikeCount(POST_ID);

        incrementViewCountTimes(ANOTHER_POST_ID, 2);
        postStatsManager.incrementCommentCount(ANOTHER_POST_ID);
    }

    private void setupPopularPostStats(Long postId) {
        incrementViewCountTimes(postId, 1000);
        incrementLikeCountTimes(postId, 50);
        incrementCommentCountTimes(postId, 10);
    }

    // 검증 메서드들
    private void assertThatStatsInitialized(Long postId) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getPostId()).isEqualTo(postId);
        assertThat(stats.getViewCount()).isEqualTo(0);
        assertThat(stats.getLikeCount()).isEqualTo(0);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    private void assertThatNoDuplicateStatsCreated(long initialCount) {
        assertThat(postStatsRepository.count()).isEqualTo(initialCount);
    }

    private void assertThatStatsExistsFor(Long postId) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getPostId()).isEqualTo(postId);
    }

    private void assertThatViewCountIs(Long postId, int expectedCount) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(expectedCount);
    }

    private void assertThatViewCountIs(PostStats stats, int expectedCount) {
        assertThat(stats.getViewCount()).isEqualTo(expectedCount);
    }

    private void assertThatLikeCountIs(Long postId, int expectedCount) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getLikeCount()).isEqualTo(expectedCount);
    }

    private void assertThatLikeCountRemainsSafe(Long postId) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getLikeCount()).isEqualTo(0);
    }

    private void assertThatCommentCountIs(Long postId, int expectedCount) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getCommentCount()).isEqualTo(expectedCount);
    }

    private void assertThatCommentCountRemainsSafe(Long postId) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    private void assertThatOnlyLikeCountChanged(Long postId) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(0);
        assertThat(stats.getCommentCount()).isEqualTo(0);
    }

    private void assertThatOnlyCommentCountChanged(Long postId) {
        PostStats stats = postStatsRepository.findByPostId(postId).orElseThrow();
        assertThat(stats.getViewCount()).isEqualTo(0);
        assertThat(stats.getLikeCount()).isEqualTo(0);
    }

    private void assertThatOperationDoesNotFail(Runnable operation) {
        assertThatCode(operation::run).doesNotThrowAnyException();
    }

    private void assertThatStatsDoesNotExistFor(Long postId) {
        assertThat(postStatsRepository.findByPostId(postId)).isEmpty();
    }

    private void assertThatStatsFound(Optional<PostStats> result) {
        assertThat(result).isPresent();
    }

    private void assertThatStatsNotFound(Optional<PostStats> result) {
        assertThat(result).isEmpty();
    }

    private void assertThatCompleteStatsCorrect(PostStats stats) {
        assertThat(stats.getPostId()).isEqualTo(POST_ID);
        assertThat(stats.getViewCount()).isEqualTo(1);
        assertThat(stats.getLikeCount()).isEqualTo(1);
        assertThat(stats.getCommentCount()).isEqualTo(1);
    }

    private void assertThatStatsFoundWithPostId(PostStats stats, Long expectedPostId) {
        assertThat(stats.getPostId()).isEqualTo(expectedPostId);
    }

    private void assertThatComprehensiveStatsCorrect(Long postId) {
        PostStats stats = postStatsManager.getPostStatsOrThrow(postId);
        assertThat(stats.getViewCount()).isEqualTo(3);
        assertThat(stats.getLikeCount()).isEqualTo(1);
        assertThat(stats.getCommentCount()).isEqualTo(1);
    }

    private void assertThatIndependentStatsCorrect() {
        PostStats stats1 = postStatsManager.getPostStatsOrThrow(POST_ID);
        assertThat(stats1.getViewCount()).isEqualTo(1);
        assertThat(stats1.getLikeCount()).isEqualTo(1);
        assertThat(stats1.getCommentCount()).isEqualTo(0);

        PostStats stats2 = postStatsManager.getPostStatsOrThrow(ANOTHER_POST_ID);
        assertThat(stats2.getViewCount()).isEqualTo(2);
        assertThat(stats2.getLikeCount()).isEqualTo(0);
        assertThat(stats2.getCommentCount()).isEqualTo(1);
    }

    private void assertThatPostIsPopular(PostStats stats) {
        assertThat(stats.isPopular()).isTrue();
        assertThat(stats.isViral()).isFalse();
    }

    private void assertThatPostHasEngagement(PostStats stats) {
        assertThat(stats.hasEngagement()).isTrue();
        assertThat(stats.getEngagementRate()).isGreaterThan(0);
    }

    private void assertThatStatsAreValid(PostStats stats) {
        assertThat(stats.hasValidStats()).isTrue();
    }
}