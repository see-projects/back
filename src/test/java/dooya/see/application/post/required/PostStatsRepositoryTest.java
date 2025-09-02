package dooya.see.application.post.required;

import dooya.see.domain.post.PostStats;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
record PostStatsRepositoryTest(PostStatsRepository postStatsRepository, EntityManager entityManager) {
    
    @DisplayName("게시물 통계를 생성하면 ID가 자동 생성되고 영속화된다")
    @Test
    void createPostStats() {
        PostStats postStats = PostStats.create(1L);

        assertThat(postStats.getId()).isNull();

        postStatsRepository.save(postStats);

        assertThat(postStats.getId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        PostStats found = entityManager.find(PostStats.class, postStats.getId());
        assertThat(found.getPostId()).isEqualTo(1L);
        assertThat(found.getViewCount()).isEqualTo(0);
        assertThat(found.getLikeCount()).isEqualTo(0);
        assertThat(found.getCommentCount()).isEqualTo(0);
    }

    @DisplayName("게시물 ID로 통계를 조회할 수 있다")
    @Test
    void findByPostId() {
        PostStats postStats = PostStats.create(1L);
        postStatsRepository.save(postStats);

        entityManager.flush();
        entityManager.clear();

        var found = postStatsRepository.findByPostId(1L);
        var notFound = postStatsRepository.findByPostId(999L);

        assertThat(found).isPresent();
        assertThat(found.get().getPostId()).isEqualTo(1L);
        assertThat(notFound).isEmpty();
    }

    @DisplayName("게시물 통계의 조회수를 증가시킬 수 있다")
    @Test
    void incrementViewCount() {
        PostStats postStats = PostStats.create(1L);
        postStatsRepository.save(postStats);

        entityManager.flush();
        entityManager.clear();

        // 통계 조회 및 조회수 증가
        PostStats found = postStatsRepository.findByPostId(1L).orElseThrow();
        found.incrementViewCount();
        postStatsRepository.save(found);

        entityManager.flush();
        entityManager.clear();

        PostStats updated = postStatsRepository.findByPostId(1L).orElseThrow();
        assertThat(updated.getViewCount()).isEqualTo(1);
    }

    @DisplayName("게시물 통계의 좋아요수를 증가시킬 수 있다")
    @Test
    void incrementLikeCount() {
        PostStats postStats = PostStats.create(1L);
        postStatsRepository.save(postStats);

        entityManager.flush();
        entityManager.clear();

        // 통계 조회 및 좋아요수 증가
        PostStats found = postStatsRepository.findByPostId(1L).orElseThrow();
        found.incrementLikeCount();
        postStatsRepository.save(found);

        entityManager.flush();
        entityManager.clear();

        PostStats updated = postStatsRepository.findByPostId(1L).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(1);
    }

    @DisplayName("게시물 통계의 좋아요수를 감소시킬 수 있다")
    @Test
    void decrementLikeCount() {
        PostStats postStats = PostStats.create(1L);
        postStats.incrementLikeCount();
        postStats.incrementLikeCount();
        postStatsRepository.save(postStats);

        entityManager.flush();
        entityManager.clear();

        // 통계 조회 및 좋아요수 감소
        PostStats found = postStatsRepository.findByPostId(1L).orElseThrow();
        found.decrementLikeCount();
        postStatsRepository.save(found);

        entityManager.flush();
        entityManager.clear();

        PostStats updated = postStatsRepository.findByPostId(1L).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(1);
    }

    @DisplayName("좋아요수는 0 미만으로 감소하지 않는다")
    @Test
    void likeCountCannotGoNegative() {
        PostStats postStats = PostStats.create(1L);
        postStatsRepository.save(postStats);

        entityManager.flush();
        entityManager.clear();

        PostStats found = postStatsRepository.findByPostId(1L).orElseThrow();
        found.decrementLikeCount();
        postStatsRepository.save(found);

        entityManager.flush();
        entityManager.clear();

        PostStats updated = postStatsRepository.findByPostId(1L).orElseThrow();
        assertThat(updated.getLikeCount()).isEqualTo(0);
    }

    @DisplayName("동일한 게시물에 대해 중복으로 통계를 생성하면 데이터 무결성 예외가 발생한다")
    @Test
    void duplicatePostStatsFail() {
        PostStats stats1 = PostStats.create(1L);
        PostStats stats2 = PostStats.create(1L);

        // 첫 번째 통계 저장 (성공해야 함)
        postStatsRepository.save(stats1);
        entityManager.flush();

        // 같은 postId로 두 번째 통계 저장 시 예외 발생해야 함
        assertThatThrownBy(() -> {
            postStatsRepository.save(stats2);
            entityManager.flush(); // flush 호출해야 실제 DB 제약조건 검사
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("서로 다른 게시물의 통계를 독립적으로 관리할 수 있다")
    @Test
    void manageMultiplePostStatsIndependently() {
        PostStats stats1 = PostStats.create(1L);
        PostStats stats2 = PostStats.create(2L);

        postStatsRepository.save(stats1);
        postStatsRepository.save(stats2);

        entityManager.flush();
        entityManager.clear();

        // 첫 번째 게시물 통계 수정
        PostStats found1 = postStatsRepository.findByPostId(1L).orElseThrow();
        found1.incrementViewCount();
        found1.incrementLikeCount();
        postStatsRepository.save(found1);

        // 두 번째 게시물 통계 수정
        PostStats found2 = postStatsRepository.findByPostId(2L).orElseThrow();
        found2.incrementViewCount();
        found2.incrementViewCount();
        postStatsRepository.save(found2);

        entityManager.flush();
        entityManager.clear();

        PostStats updated1 = postStatsRepository.findByPostId(1L).orElseThrow();
        PostStats updated2 = postStatsRepository.findByPostId(2L).orElseThrow();

        assertThat(updated1.getViewCount()).isEqualTo(1);
        assertThat(updated1.getLikeCount()).isEqualTo(1);
        assertThat(updated2.getViewCount()).isEqualTo(2);
        assertThat(updated2.getLikeCount()).isEqualTo(0);
    }

    @DisplayName("통계 데이터의 기본 속성들이 올바르게 설정된다")
    @Test
    void basicStatsPropertiesAreSet() {
        PostStats postStats = PostStats.create(1L);
        postStatsRepository.save(postStats);

        entityManager.flush();
        entityManager.clear();

        PostStats found = postStatsRepository.findByPostId(1L).orElseThrow();

        assertThat(found.hasValidStats()).isTrue();
        assertThat(found.isPopular()).isFalse();
        assertThat(found.isViral()).isFalse();
        assertThat(found.hasEngagement()).isFalse();
        assertThat(found.getEngagementRate()).isEqualTo(0.0);
    }

    @DisplayName("대량의 통계 업데이트가 가능하다")
    @Test
    void handleMassiveStatsUpdates() {
        PostStats postStats = PostStats.create(1L);
        postStatsRepository.save(postStats);

        entityManager.flush();
        entityManager.clear();

        // 100번의 조회수 증가
        for (int i = 0; i < 100; i++) {
            PostStats found = postStatsRepository.findByPostId(1L).orElseThrow();
            found.incrementViewCount();
            postStatsRepository.save(found);
            
            entityManager.flush();
            entityManager.clear();
        }

        PostStats finalStats = postStatsRepository.findByPostId(1L).orElseThrow();
        assertThat(finalStats.getViewCount()).isEqualTo(100);
    }

    @DisplayName("통계 조회 시 해당 게시물이 없으면 빈 결과를 반환한다")
    @Test
    void findByNonExistentPostId() {
        var result = postStatsRepository.findByPostId(999L);

        assertThat(result).isEmpty();
    }
}
