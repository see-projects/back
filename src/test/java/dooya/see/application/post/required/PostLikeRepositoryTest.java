package dooya.see.application.post.required;

import dooya.see.domain.post.PostLike;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DataJpaTest
record PostLikeRepositoryTest(PostLikeRepository postLikeRepository, EntityManager entityManager) {
    @Test
    void 게시물_좋아요를_생성하면_ID가_자동_생성되고_영속화된다() {
        PostLike postLike = PostLike.create(1L, 100L);

        assertThat(postLike.getId()).isNull();

        postLikeRepository.save(postLike);

        assertThat(postLike.getId()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        PostLike found = entityManager.find(PostLike.class, postLike.getId());
        assertThat(found.getPostId()).isEqualTo(1L);
        assertThat(found.getMemberId()).isEqualTo(100L);
        assertThat(found.getLikedAt()).isNotNull();
    }

    @Test
    void 특정_게시물에_특정_회원이_좋아요를_눌렀는지_확인할_수_있다() {
        PostLike postLike = PostLike.create(1L, 100L);
        postLikeRepository.save(postLike);

        entityManager.flush();
        entityManager.clear();

        boolean exists = postLikeRepository.existsByPostIdAndMemberId(1L, 100L);
        boolean notExists = postLikeRepository.existsByPostIdAndMemberId(1L, 200L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void 특정_게시물의_좋아요_개수를_조회할_수_있다() {
        PostLike like1 = PostLike.create(1L, 100L);
        PostLike like2 = PostLike.create(1L, 200L);
        PostLike like3 = PostLike.create(2L, 100L);

        postLikeRepository.save(like1);
        postLikeRepository.save(like2);
        postLikeRepository.save(like3);

        entityManager.flush();
        entityManager.clear();

        long count1 = postLikeRepository.countByPostId(1L);
        long count2 = postLikeRepository.countByPostId(2L);
        long count3 = postLikeRepository.countByPostId(999L);

        assertThat(count1).isEqualTo(2);
        assertThat(count2).isEqualTo(1);
        assertThat(count3).isEqualTo(0);
    }

    @Test
    void 특정_게시물과_회원의_좋아요를_삭제할_수_있다() {
        PostLike like1 = PostLike.create(1L, 100L);
        PostLike like2 = PostLike.create(1L, 200L);
        PostLike like3 = PostLike.create(2L, 100L);

        postLikeRepository.save(like1);
        postLikeRepository.save(like2);
        postLikeRepository.save(like3);

        entityManager.flush();
        entityManager.clear();

        postLikeRepository.deleteByPostIdAndMemberId(1L, 100L);

        entityManager.flush();
        entityManager.clear();

        boolean deletedExists = postLikeRepository.existsByPostIdAndMemberId(1L, 100L);
        boolean otherExists = postLikeRepository.existsByPostIdAndMemberId(1L, 200L);
        boolean anotherPostExists = postLikeRepository.existsByPostIdAndMemberId(2L, 100L);

        assertThat(deletedExists).isFalse();
        assertThat(otherExists).isTrue();
        assertThat(anotherPostExists).isTrue();
    }

    @Test
    void 존재하지_않는_좋아요를_삭제해도_오류가_발생하지_않는다() {
        assertThatNoException()
            .isThrownBy(() -> postLikeRepository.deleteByPostIdAndMemberId(999L, 999L));
    }

    @Test
    void 동일한_게시물에_동일한_회원이_중복_좋아요를_누르는_것을_허용한다() {
        PostLike like1 = PostLike.create(1L, 100L);
        PostLike like2 = PostLike.create(1L, 100L);

        postLikeRepository.save(like1);
        postLikeRepository.save(like2);

        entityManager.flush();
        entityManager.clear();

        long count = postLikeRepository.countByPostId(1L);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 서로_다른_회원이_같은_게시물에_좋아요를_누를_수_있다() {
        PostLike like1 = PostLike.create(1L, 100L);
        PostLike like2 = PostLike.create(1L, 200L);
        PostLike like3 = PostLike.create(1L, 300L);

        postLikeRepository.save(like1);
        postLikeRepository.save(like2);
        postLikeRepository.save(like3);

        entityManager.flush();
        entityManager.clear();

        long count = postLikeRepository.countByPostId(1L);

        assertThat(count).isEqualTo(3);
        assertThat(postLikeRepository.existsByPostIdAndMemberId(1L, 100L)).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndMemberId(1L, 200L)).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndMemberId(1L, 300L)).isTrue();
    }

    @Test
    void 한_회원이_서로_다른_게시물에_좋아요를_누를_수_있다() {
        PostLike like1 = PostLike.create(1L, 100L);
        PostLike like2 = PostLike.create(2L, 100L);
        PostLike like3 = PostLike.create(3L, 100L);

        postLikeRepository.save(like1);
        postLikeRepository.save(like2);
        postLikeRepository.save(like3);

        entityManager.flush();
        entityManager.clear();

        assertThat(postLikeRepository.existsByPostIdAndMemberId(1L, 100L)).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndMemberId(2L, 100L)).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndMemberId(3L, 100L)).isTrue();
    }

    @Test
    void 좋아요_삭제_후_다시_좋아요를_누를_수_있다() {
        PostLike originalLike = PostLike.create(1L, 100L);
        postLikeRepository.save(originalLike);

        entityManager.flush();
        entityManager.clear();

        // 좋아요 삭제
        postLikeRepository.deleteByPostIdAndMemberId(1L, 100L);
        
        entityManager.flush();
        entityManager.clear();

        assertThat(postLikeRepository.existsByPostIdAndMemberId(1L, 100L)).isFalse();

        // 다시 좋아요
        PostLike newLike = PostLike.create(1L, 100L);
        postLikeRepository.save(newLike);

        entityManager.flush();
        entityManager.clear();

        assertThat(postLikeRepository.existsByPostIdAndMemberId(1L, 100L)).isTrue();
        assertThat(postLikeRepository.countByPostId(1L)).isEqualTo(1);
    }

    @Test
    void 대량의_좋아요_데이터_처리가_가능하다() {
        // 100개의 좋아요 생성
        for (int i = 1; i <= 100; i++) {
            PostLike like = PostLike.create(1L, (long) i);
            postLikeRepository.save(like);
        }

        entityManager.flush();
        entityManager.clear();

        long count = postLikeRepository.countByPostId(1L);

        assertThat(count).isEqualTo(100);
    }
}
