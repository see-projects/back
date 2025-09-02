package dooya.see.application.post.required;

import dooya.see.domain.post.PostLike;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
record PostLikeRepositoryTest(PostLikeRepository postLikeRepository, EntityManager entityManager) {
    
    @DisplayName("게시물 좋아요를 생성하면 ID가 자동 생성되고 영속화된다")
    @Test
    void createPostLike() {
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

    @DisplayName("특정 게시물에 특정 회원이 좋아요를 눌렀는지 확인할 수 있다")
    @Test
    void existsByPostIdAndMemberId() {
        PostLike postLike = PostLike.create(1L, 100L);
        postLikeRepository.save(postLike);

        entityManager.flush();
        entityManager.clear();

        boolean exists = postLikeRepository.existsByPostIdAndMemberId(1L, 100L);
        boolean notExists = postLikeRepository.existsByPostIdAndMemberId(1L, 200L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @DisplayName("특정 게시물의 좋아요 개수를 조회할 수 있다")
    @Test
    void countByPostId() {
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

    @DisplayName("특정 게시물과 회원의 좋아요를 삭제할 수 있다")
    @Test
    void deleteByPostIdAndMemberId() {
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

    @DisplayName("존재하지 않는 좋아요를 삭제해도 오류가 발생하지 않는다")
    @Test
    void deleteNonExistentLike() {
        assertThatNoException()
            .isThrownBy(() -> postLikeRepository.deleteByPostIdAndMemberId(999L, 999L));
    }

    @DisplayName("동일한 게시물에 동일한 회원이 중복 좋아요를 누르는 것을 허용한다")
    @Test
    void allowDuplicateLike() {
        PostLike like1 = PostLike.create(1L, 100L);
        PostLike like2 = PostLike.create(1L, 100L);

        postLikeRepository.save(like1);
        postLikeRepository.save(like2);

        entityManager.flush();
        entityManager.clear();

        long count = postLikeRepository.countByPostId(1L);
        assertThat(count).isEqualTo(2);
    }

    @DisplayName("서로 다른 회원이 같은 게시물에 좋아요를 누를 수 있다")
    @Test
    void multipleMembersCanLikeSamePost() {
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

    @DisplayName("한 회원이 서로 다른 게시물에 좋아요를 누를 수 있다")
    @Test
    void sameMemberCanLikeMultiplePosts() {
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

    @DisplayName("좋아요 삭제 후 다시 좋아요를 누를 수 있다")
    @Test
    void likeAfterUnlike() {
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

    @DisplayName("대량의 좋아요 데이터 처리가 가능하다")
    @Test
    void handleMassiveLikes() {
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
