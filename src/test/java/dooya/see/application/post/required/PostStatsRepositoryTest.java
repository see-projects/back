package dooya.see.application.post.required;

import dooya.see.domain.post.PostStats;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
record PostStatsRepositoryTest(PostStatsRepository postStatsRepository, EntityManager entityManager) {
    private static final Long POST_ID = 1L;
    private static final Long ANOTHER_POST_ID = 2L;
    private static final Long NON_EXISTENT_POST_ID = 999L;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 통계_생성_및_조회 {
        @Test
        void 통계_생성이_성공한다() {
            PostStats postStats = createTestPostStats(POST_ID);

            assertThatPostStatsCreated(postStats, POST_ID);
        }

        @Test
        void 게시물_ID로_통계를_조회할_수_있다() {
            createAndSavePostStats(POST_ID);

            var found = postStatsRepository.findByPostId(POST_ID);
            var notFound = postStatsRepository.findByPostId(NON_EXISTENT_POST_ID);

            assertThatStatsFound(found, POST_ID);
            assertThat(notFound).isEmpty();
        }

        @Test
        void 존재하지_않는_게시물_통계_조회_시_빈_결과를_반환한다() {
            var result = postStatsRepository.findByPostId(NON_EXISTENT_POST_ID);

            assertThat(result).isEmpty();
        }

        private void assertThatPostStatsCreated(PostStats postStats, Long expectedPostId) {
            assertThat(postStats.getId()).isNotNull();

            flushAndClearContext();
            PostStats found = entityManager.find(PostStats.class, postStats.getId());

            assertThat(found.getPostId()).isEqualTo(expectedPostId);
            assertThat(found.getViewCount()).isEqualTo(0);
            assertThat(found.getLikeCount()).isEqualTo(0);
            assertThat(found.getCommentCount()).isEqualTo(0);
        }

        private void assertThatStatsFound(java.util.Optional<PostStats> found, Long expectedPostId) {
            assertThat(found).isPresent();
            assertThat(found.get().getPostId()).isEqualTo(expectedPostId);
        }
    }

    @Nested
    class 조회수_관리 {
        @Test
        void 조회수_증가가_성공한다() {
            createAndSavePostStats(POST_ID);

            PostStats updated = incrementViewCountAndSave(POST_ID);

            assertThat(updated.getViewCount()).isEqualTo(1);
        }

        @Test
        void 대량의_조회수_증가가_가능하다() {
            createAndSavePostStats(POST_ID);
            int incrementCount = 100;

            for (int i = 0; i < incrementCount; i++) {
                incrementViewCountAndSave(POST_ID);
            }

            PostStats finalStats = postStatsRepository.findByPostId(POST_ID).orElseThrow();
            assertThat(finalStats.getViewCount()).isEqualTo(incrementCount);
        }

        private PostStats incrementViewCountAndSave(Long postId) {
            PostStats found = postStatsRepository.findByPostId(postId).orElseThrow();
            found.incrementViewCount();
            PostStats saved = postStatsRepository.save(found);
            flushAndClearContext();
            return postStatsRepository.findByPostId(postId).orElseThrow();
        }
    }

    @Nested
    class 좋아요수_관리 {
        @Test
        void 좋아요수_증가가_성공한다() {
            createAndSavePostStats(POST_ID);

            PostStats updated = incrementLikeCountAndSave(POST_ID);

            assertThat(updated.getLikeCount()).isEqualTo(1);
        }

        @Test
        void 좋아요수_감소가_성공한다() {
            PostStats postStats = createPostStatsWithLikes(POST_ID, 2);
            postStatsRepository.save(postStats);
            flushAndClearContext();

            PostStats updated = decrementLikeCountAndSave(POST_ID);

            assertThat(updated.getLikeCount()).isEqualTo(1);
        }

        @Test
        void 좋아요수는_0_미만으로_감소하지_않는다() {
            createAndSavePostStats(POST_ID);

            PostStats updated = decrementLikeCountAndSave(POST_ID);

            assertThat(updated.getLikeCount()).isEqualTo(0);
        }

        private PostStats incrementLikeCountAndSave(Long postId) {
            PostStats found = postStatsRepository.findByPostId(postId).orElseThrow();
            found.incrementLikeCount();
            PostStats saved = postStatsRepository.save(found);
            flushAndClearContext();
            return postStatsRepository.findByPostId(postId).orElseThrow();
        }

        private PostStats decrementLikeCountAndSave(Long postId) {
            PostStats found = postStatsRepository.findByPostId(postId).orElseThrow();
            found.decrementLikeCount();
            PostStats saved = postStatsRepository.save(found);
            flushAndClearContext();
            return postStatsRepository.findByPostId(postId).orElseThrow();
        }

        private PostStats createPostStatsWithLikes(Long postId, int likeCount) {
            PostStats postStats = PostStats.create(postId);
            for (int i = 0; i < likeCount; i++) {
                postStats.incrementLikeCount();
            }
            return postStats;
        }
    }

    @Nested
    class 데이터_무결성 {
        @Test
        void 동일_게시물의_중복_통계_생성_시_예외가_발생한다() {
            PostStats stats1 = PostStats.create(POST_ID);
            PostStats stats2 = PostStats.create(POST_ID);

            postStatsRepository.save(stats1);
            entityManager.flush();

            assertThatThrownBy(() -> {
                postStatsRepository.save(stats2);
                entityManager.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 서로_다른_게시물의_통계를_독립적으로_관리할_수_있다() {
            PostStats stats1 = createAndSavePostStats(POST_ID);
            PostStats stats2 = createAndSavePostStats(ANOTHER_POST_ID);

            updateFirstPostStats(POST_ID);
            updateSecondPostStats(ANOTHER_POST_ID);

            assertThatIndependentStatsUpdated();
        }

        private void updateFirstPostStats(Long postId) {
            PostStats found1 = postStatsRepository.findByPostId(postId).orElseThrow();
            found1.incrementViewCount();
            found1.incrementLikeCount();
            postStatsRepository.save(found1);
            flushAndClearContext();
        }

        private void updateSecondPostStats(Long postId) {
            PostStats found2 = postStatsRepository.findByPostId(postId).orElseThrow();
            found2.incrementViewCount();
            found2.incrementViewCount();
            postStatsRepository.save(found2);
            flushAndClearContext();
        }

        private void assertThatIndependentStatsUpdated() {
            PostStats updated1 = postStatsRepository.findByPostId(POST_ID).orElseThrow();
            PostStats updated2 = postStatsRepository.findByPostId(ANOTHER_POST_ID).orElseThrow();

            assertThat(updated1.getViewCount()).isEqualTo(1);
            assertThat(updated1.getLikeCount()).isEqualTo(1);
            assertThat(updated2.getViewCount()).isEqualTo(2);
            assertThat(updated2.getLikeCount()).isEqualTo(0);
        }
    }

    @Nested
    class 통계_속성_검증 {
        @Test
        void 통계_기본_속성들이_올바르게_설정된다() {
            createAndSavePostStats(POST_ID);

            PostStats found = postStatsRepository.findByPostId(POST_ID).orElseThrow();

            assertThatDefaultPropertiesValid(found);
        }

        private void assertThatDefaultPropertiesValid(PostStats found) {
            assertThat(found.hasValidStats()).isTrue();
            assertThat(found.isPopular()).isFalse();
            assertThat(found.isViral()).isFalse();
            assertThat(found.hasEngagement()).isFalse();
            assertThat(found.getEngagementRate()).isEqualTo(0.0);
        }
    }

    // 헬퍼 메서드들
    private PostStats createTestPostStats(Long postId) {
        PostStats postStats = PostStats.create(postId);
        assertThat(postStats.getId()).isNull();

        postStatsRepository.save(postStats);
        assertThat(postStats.getId()).isNotNull();

        return postStats;
    }

    private PostStats createAndSavePostStats(Long postId) {
        PostStats postStats = PostStats.create(postId);
        PostStats saved = postStatsRepository.save(postStats);
        flushAndClearContext();
        return saved;
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}