package dooya.see.application.post.required;

import dooya.see.domain.post.PostLike;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
record PostLikeRepositoryTest(PostLikeRepository postLikeRepository, EntityManager entityManager) {
    private static final Long POST_ID = 1L;
    private static final Long ANOTHER_POST_ID = 2L;
    private static final Long MEMBER_ID = 100L;
    private static final Long ANOTHER_MEMBER_ID = 200L;
    private static final Long THIRD_MEMBER_ID = 300L;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 좋아요_생성_및_조회 {
        @Test
        void 좋아요_생성이_성공한다() {
            PostLike postLike = createTestPostLike(POST_ID, MEMBER_ID);

            assertThatPostLikeCreated(postLike, POST_ID, MEMBER_ID);
        }

        @Test
        void 좋아요_존재_여부를_확인할_수_있다() {
            createAndSavePostLike(POST_ID, MEMBER_ID);

            boolean exists = postLikeRepository.existsByPostIdAndMemberId(POST_ID, MEMBER_ID);
            boolean notExists = postLikeRepository.existsByPostIdAndMemberId(POST_ID, ANOTHER_MEMBER_ID);

            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        void 게시물별_좋아요_개수를_조회할_수_있다() {
            createAndSavePostLike(POST_ID, MEMBER_ID);
            createAndSavePostLike(POST_ID, ANOTHER_MEMBER_ID);
            createAndSavePostLike(ANOTHER_POST_ID, MEMBER_ID);

            long firstPostCount = postLikeRepository.countByPostId(POST_ID);
            long secondPostCount = postLikeRepository.countByPostId(ANOTHER_POST_ID);
            long nonExistentPostCount = postLikeRepository.countByPostId(999L);

            assertThatLikeCountsMatch(firstPostCount, 2, secondPostCount, 1, nonExistentPostCount, 0);
        }

        private void assertThatPostLikeCreated(PostLike postLike, Long expectedPostId, Long expectedMemberId) {
            assertThat(postLike.getId()).isNotNull();

            flushAndClearContext();
            PostLike found = entityManager.find(PostLike.class, postLike.getId());

            assertThat(found.getPostId()).isEqualTo(expectedPostId);
            assertThat(found.getMemberId()).isEqualTo(expectedMemberId);
            assertThat(found.getLikedAt()).isNotNull();
        }

        private void assertThatLikeCountsMatch(long firstPostCount, int expectedFirst,
                                               long secondPostCount, int expectedSecond,
                                               long nonExistentPostCount, int expectedNonExistent) {
            assertThat(firstPostCount).isEqualTo(expectedFirst);
            assertThat(secondPostCount).isEqualTo(expectedSecond);
            assertThat(nonExistentPostCount).isEqualTo(expectedNonExistent);
        }
    }

    @Nested
    class 좋아요_삭제 {
        @Test
        void 좋아요_삭제가_성공한다() {
            createAndSavePostLike(POST_ID, MEMBER_ID);
            createAndSavePostLike(POST_ID, ANOTHER_MEMBER_ID);
            createAndSavePostLike(ANOTHER_POST_ID, MEMBER_ID);

            postLikeRepository.deleteByPostIdAndMemberId(POST_ID, MEMBER_ID);
            flushAndClearContext();

            assertThatSpecificLikeDeleted(POST_ID, MEMBER_ID);
            assertThatOtherLikesRemain(POST_ID, ANOTHER_MEMBER_ID, ANOTHER_POST_ID, MEMBER_ID);
        }

        @Test
        void 존재하지_않는_좋아요_삭제_시_예외가_발생하지_않는다() {
            assertThatNoException()
                    .isThrownBy(() -> postLikeRepository.deleteByPostIdAndMemberId(999L, 999L));
        }

        @Test
        void 좋아요_삭제_후_재생성이_가능하다() {
            createAndSavePostLike(POST_ID, MEMBER_ID);

            postLikeRepository.deleteByPostIdAndMemberId(POST_ID, MEMBER_ID);
            flushAndClearContext();

            assertThat(postLikeRepository.existsByPostIdAndMemberId(POST_ID, MEMBER_ID)).isFalse();

            createAndSavePostLike(POST_ID, MEMBER_ID);

            assertThatLikeRecreated(POST_ID, MEMBER_ID);
        }

        private void assertThatSpecificLikeDeleted(Long postId, Long memberId) {
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).isFalse();
        }

        private void assertThatOtherLikesRemain(Long postId, Long remainingMemberId,
                                                Long anotherPostId, Long anotherMemberId) {
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, remainingMemberId)).isTrue();
            assertThat(postLikeRepository.existsByPostIdAndMemberId(anotherPostId, anotherMemberId)).isTrue();
        }

        private void assertThatLikeRecreated(Long postId, Long memberId) {
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).isTrue();
            assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(1);
        }
    }

    @Nested
    class 좋아요_중복_처리 {
        @Test
        void 동일_게시물에_동일_회원의_중복_좋아요는_허용되지_않는다() {
            createAndSavePostLike(POST_ID, MEMBER_ID);

            assertThatThrownBy(() -> createAndSavePostLike(POST_ID, MEMBER_ID))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void 여러_회원이_동일_게시물에_좋아요를_누를_수_있다() {
            createAndSavePostLike(POST_ID, MEMBER_ID);
            createAndSavePostLike(POST_ID, ANOTHER_MEMBER_ID);
            createAndSavePostLike(POST_ID, THIRD_MEMBER_ID);

            assertThatMultipleMembersLikedSamePost(POST_ID, MEMBER_ID, ANOTHER_MEMBER_ID, THIRD_MEMBER_ID);
        }

        @Test
        void 동일_회원이_여러_게시물에_좋아요를_누를_수_있다() {
            Long thirdPostId = 3L;
            createAndSavePostLike(POST_ID, MEMBER_ID);
            createAndSavePostLike(ANOTHER_POST_ID, MEMBER_ID);
            createAndSavePostLike(thirdPostId, MEMBER_ID);

            assertThatSameMemberLikedMultiplePosts(MEMBER_ID, POST_ID, ANOTHER_POST_ID, thirdPostId);
        }

        private void assertThatMultipleMembersLikedSamePost(Long postId, Long... memberIds) {
            assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(memberIds.length);

            for (Long memberId : memberIds) {
                assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).isTrue();
            }
        }

        private void assertThatSameMemberLikedMultiplePosts(Long memberId, Long... postIds) {
            for (Long postId : postIds) {
                assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).isTrue();
            }
        }
    }

    @Nested
    class 대량_데이터_처리 {
        @Test
        void 대량의_좋아요_데이터_처리가_가능하다() {
            int likeCount = 100;

            createMultipleLikes(POST_ID, likeCount);

            long count = postLikeRepository.countByPostId(POST_ID);

            assertThat(count).isEqualTo(likeCount);
        }

        private void createMultipleLikes(Long postId, int count) {
            for (int i = 1; i <= count; i++) {
                PostLike like = PostLike.create(postId, (long) i);
                postLikeRepository.save(like);
            }
            flushAndClearContext();
        }
    }

    // 헬퍼 메서드들
    private PostLike createTestPostLike(Long postId, Long memberId) {
        PostLike postLike = PostLike.create(postId, memberId);
        assertThat(postLike.getId()).isNull();

        postLikeRepository.save(postLike);
        assertThat(postLike.getId()).isNotNull();

        return postLike;
    }

    private PostLike createAndSavePostLike(Long postId, Long memberId) {
        PostLike postLike = PostLike.create(postId, memberId);
        postLikeRepository.save(postLike);
        flushAndClearContext();
        return postLike;
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
