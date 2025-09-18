package dooya.see.application.post.required;

import dooya.see.domain.post.PostStats;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
record PostStatsRepositoryTest(PostStatsRepository postStatsRepository, EntityManager entityManager) {
    @Test
    void 게시물_통계를_생성하면_ID가_자동_생성되고_영속화된다() {
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

    @Test
    void 게시물_ID로_통계를_조회할_수_있다() {
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

    @Test
    void 게시물_통계의_조회수를_증가시킬_수_있다() {
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

    @Test
    void 게시물_통계의_좋아요수를_증가시킬_수_있다() {
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

    @Test
    void 게시물_통계의_좋아요수를_감소시킬_수_있다() {
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

    @Test
    void 좋아요수는_0_미만으로_감소하지_않는다() {
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

    @Test
    void 동일한_게시물에_대해_중복으로_통계를_생성하면_데이터_무결성_예외가_발생한다() {
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

    @Test
    void 서로_다른_게시물의_통계를_독립적으로_관리할_수_있다() {
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

    @Test
    void 통계_데이터의_기본_속성들이_올바르게_설정된다() {
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

    @Test
    void 대량의_통계_업데이트가_가능하다() {
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

    @Test
    void 통계_조회_시_해당_게시물이_없으면_빈_결과를_반환한다() {
        var result = postStatsRepository.findByPostId(999L);

        assertThat(result).isEmpty();
    }
}
