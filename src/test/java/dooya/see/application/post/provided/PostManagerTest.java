package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.PostLikeRepository;
import dooya.see.domain.post.*;
import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import dooya.see.domain.post.exception.PostNotFoundException;
import dooya.see.domain.post.exception.UnauthorizedPostAccessException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Import(SeeTestConfiguration.class)
record PostManagerTest(PostManager postManager, EntityManager entityManager, PostFinder postFinder, ApplicationEventPublisher eventPublisher, PostLikeRepository postLikeRepository) {
    private static final Long AUTHOR_ID = 1L;
    private static final Long UNAUTHORIZED_USER_ID = 2L;
    private static final Long ANOTHER_MEMBER_ID = 3L;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Nested
    class 게시글_생성 {
        @Test
        void 게시글_생성이_성공한다() {
            Post post = createTestPost();

            assertThatPostCreated(post);
        }

        private void assertThatPostCreated(Post post) {
            assertThat(post.getId()).isNotNull();
            assertThat(post.getMemberId()).isEqualTo(AUTHOR_ID);
            assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
            assertThat(post.getMetaData()).isNotNull();
        }
    }

    @Nested
    class 게시글_수정 {
        @Test
        void 모든_필드_수정이_성공한다() {
            Post post = createTestPost();

            Post updatedPost = postManager.update(updateAllFieldsRequest(), post.getId(), AUTHOR_ID);

            assertThatAllFieldsUpdated(updatedPost);
        }

        @Test
        void 제목만_수정이_성공한다() {
            Post post = createTestPost();
            String originalBody = post.getContent().body();
            PostCategory originalCategory = post.getCategory();

            Post updatedPost = postManager.update(updateTitleOnlyRequest(), post.getId(), AUTHOR_ID);

            assertThatTitleOnlyUpdated(updatedPost, originalBody, originalCategory);
        }

        @Test
        void 내용만_수정이_성공한다() {
            Post post = createTestPost();
            String originalTitle = post.getContent().title();
            PostCategory originalCategory = post.getCategory();

            Post updatedPost = postManager.update(updateBodyOnlyRequest(), post.getId(), AUTHOR_ID);

            assertThatBodyOnlyUpdated(updatedPost, originalTitle, originalCategory);
        }

        @Test
        void 카테고리만_수정이_성공한다() {
            Post post = createTestPost();
            String originalTitle = post.getContent().title();
            String originalBody = post.getContent().body();

            Post updatedPost = postManager.update(updateCategoryOnlyRequest(), post.getId(), AUTHOR_ID);

            assertThatCategoryOnlyUpdated(updatedPost, originalTitle, originalBody);
        }

        @Test
        void 변경사항이_없는_요청으로_수정_시_예외가_발생한다() {
            Post post = createTestPost();

            assertThatThrownBy(() -> postManager.update(noUpdateRequest(), post.getId(), AUTHOR_ID))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 존재하지_않는_게시글_수정_시_예외가_발생한다() {
            assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), 999L, AUTHOR_ID))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        void 다른_사용자의_게시글_수정_시_예외가_발생한다() {
            Post post = createTestPost();

            assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), post.getId(), UNAUTHORIZED_USER_ID))
                    .isInstanceOf(UnauthorizedPostAccessException.class);
        }

        private void assertThatAllFieldsUpdated(Post updatedPost) {
            assertThat(updatedPost.getContent().title()).isEqualTo("수정된 제목");
            assertThat(updatedPost.getContent().body()).isEqualTo("수정된 내용");
            assertThat(updatedPost.getCategory()).isEqualTo(PostCategory.QNA);
        }

        private void assertThatTitleOnlyUpdated(Post updatedPost, String originalBody, PostCategory originalCategory) {
            assertThat(updatedPost.getContent().title()).isEqualTo("새로운 제목");
            assertThat(updatedPost.getContent().body()).isEqualTo(originalBody);
            assertThat(updatedPost.getCategory()).isEqualTo(originalCategory);
        }

        private void assertThatBodyOnlyUpdated(Post updatedPost, String originalTitle, PostCategory originalCategory) {
            assertThat(updatedPost.getContent().title()).isEqualTo(originalTitle);
            assertThat(updatedPost.getContent().body()).isEqualTo("새로운 내용");
            assertThat(updatedPost.getCategory()).isEqualTo(originalCategory);
        }

        private void assertThatCategoryOnlyUpdated(Post updatedPost, String originalTitle, String originalBody) {
            assertThat(updatedPost.getContent().title()).isEqualTo(originalTitle);
            assertThat(updatedPost.getContent().body()).isEqualTo(originalBody);
            assertThat(updatedPost.getCategory()).isEqualTo(PostCategory.NOTICE);
        }
    }

    @Nested
    class 게시글_발행 {
        @Test
        void 게시글_발행이_성공한다() {
            Post post = createTestPost();

            Post publishedPost = postManager.publish(post.getId(), AUTHOR_ID);

            assertThatPostPublished(publishedPost);
        }

        @Test
        void 이미_발행된_게시글_재발행_시_예외가_발생한다() {
            Post post = createTestPost();
            postManager.publish(post.getId(), AUTHOR_ID);

            assertThatThrownBy(() -> postManager.publish(post.getId(), AUTHOR_ID))
                    .isInstanceOf(InvalidPostStatusTransitionException.class);
        }

        @Test
        void 숨김_처리된_게시글_발행이_성공한다() {
            Post post = createAndPublishPost();
            postManager.hide(post.getId(), AUTHOR_ID);

            Post republishedPost = postManager.publish(post.getId(), AUTHOR_ID);

            assertThatPostPublished(republishedPost);
        }

        private void assertThatPostPublished(Post publishedPost) {
            assertThat(publishedPost.getStatus()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(publishedPost.getMetaData().publishedAt()).isNotNull();
        }
    }

    @Nested
    class 게시글_숨김 {
        @Test
        void 발행된_게시글_숨김이_성공한다() {
            Post post = createAndPublishPost();

            Post hiddenPost = postManager.hide(post.getId(), AUTHOR_ID);

            assertThatPostHidden(hiddenPost);
        }

        @Test
        void 초안_게시글_숨김이_성공한다() {
            Post post = createTestPost();

            Post hiddenPost = postManager.hide(post.getId(), AUTHOR_ID);

            assertThatPostHidden(hiddenPost);
        }

        @Test
        void 이미_숨김_처리된_게시글_재숨김_시_예외가_발생한다() {
            Post post = createAndPublishPost();
            postManager.hide(post.getId(), AUTHOR_ID);

            assertThatThrownBy(() -> postManager.hide(post.getId(), AUTHOR_ID))
                    .isInstanceOf(InvalidPostStatusTransitionException.class);
        }

        private void assertThatPostHidden(Post hiddenPost) {
            assertThat(hiddenPost.getStatus()).isEqualTo(PostStatus.HIDDEN);
        }
    }

    @Nested
    class 게시글_삭제 {
        @Test
        void 게시글_삭제가_성공한다() {
            Post post = createTestPost();

            Post deletedPost = postManager.delete(post.getId(), AUTHOR_ID);

            assertThatPostDeleted(deletedPost);
        }

        @Test
        void 이미_삭제된_게시글_재삭제_시_예외가_발생한다() {
            Post post = createTestPost();
            postManager.delete(post.getId(), AUTHOR_ID);

            assertThatThrownBy(() -> postManager.delete(post.getId(), AUTHOR_ID))
                    .isInstanceOf(InvalidPostStatusTransitionException.class);
        }

        private void assertThatPostDeleted(Post deletedPost) {
            assertThat(deletedPost.getStatus()).isEqualTo(PostStatus.DELETED);
        }
    }

    @Nested
    class 게시글_좋아요 {
        @Test
        void 게시글_좋아요가_성공한다() {
            Post post = createTestPost();

            Post likedPost = postManager.likePost(post.getId(), ANOTHER_MEMBER_ID);

            assertThatPostLiked(likedPost, ANOTHER_MEMBER_ID);
        }

        @Test
        void 동일_회원의_중복_좋아요는_멱등성이_보장된다() {
            Post post = createTestPost();
            postManager.likePost(post.getId(), ANOTHER_MEMBER_ID);

            postManager.likePost(post.getId(), ANOTHER_MEMBER_ID);

            assertThatLikeCountRemainsSame(post.getId(), 1);
        }

        @Test
        void 여러_회원의_좋아요가_누적된다() {
            Post post = createTestPost();
            Long member1 = 10L;
            Long member2 = 20L;
            Long member3 = 30L;

            postManager.likePost(post.getId(), member1);
            postManager.likePost(post.getId(), member2);
            postManager.likePost(post.getId(), member3);

            assertThatMultipleLikesAccumulated(post.getId(), member1, member2, member3);
        }

        @Test
        void 존재하지_않는_게시글_좋아요_시_예외가_발생한다() {
            assertThatThrownBy(() -> postManager.likePost(999L, ANOTHER_MEMBER_ID))
                    .isInstanceOf(PostNotFoundException.class);
        }

        private void assertThatPostLiked(Post likedPost, Long memberId) {
            assertThat(likedPost.getId()).isNotNull();
            assertThat(postLikeRepository.existsByPostIdAndMemberId(likedPost.getId(), memberId)).isTrue();
            assertThat(postLikeRepository.countByPostId(likedPost.getId())).isEqualTo(1);
        }

        private void assertThatLikeCountRemainsSame(Long postId, long expectedCount) {
            assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(expectedCount);
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, ANOTHER_MEMBER_ID)).isTrue();
        }

        private void assertThatMultipleLikesAccumulated(Long postId, Long member1, Long member2, Long member3) {
            assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(3);
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, member1)).isTrue();
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, member2)).isTrue();
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, member3)).isTrue();
        }
    }

    @Nested
    class 게시글_좋아요_취소 {
        @Test
        void 게시글_좋아요_취소가_성공한다() {
            Post post = createTestPost();
            postManager.likePost(post.getId(), ANOTHER_MEMBER_ID);

            Post unlikedPost = postManager.unlikePost(post.getId(), ANOTHER_MEMBER_ID);

            assertThatPostUnliked(unlikedPost, ANOTHER_MEMBER_ID);
        }

        @Test
        void 존재하지_않는_좋아요_취소는_멱등성이_보장된다() {
            Post post = createTestPost();

            postManager.unlikePost(post.getId(), ANOTHER_MEMBER_ID);

            assertThatLikeNotExists(post.getId(), ANOTHER_MEMBER_ID);
        }

        @Test
        void 좋아요_취소_후_재좋아요가_가능하다() {
            Post post = createTestPost();
            postManager.likePost(post.getId(), ANOTHER_MEMBER_ID);
            postManager.unlikePost(post.getId(), ANOTHER_MEMBER_ID);

            postManager.likePost(post.getId(), ANOTHER_MEMBER_ID);

            assertThatPostLiked(post, ANOTHER_MEMBER_ID);
        }

        @Test
        void 존재하지_않는_게시글_좋아요_취소_시_예외가_발생한다() {
            assertThatThrownBy(() -> postManager.unlikePost(999L, ANOTHER_MEMBER_ID))
                    .isInstanceOf(PostNotFoundException.class);
        }

        private void assertThatPostUnliked(Post unlikedPost, Long memberId) {
            assertThat(unlikedPost.getId()).isNotNull();
            assertThat(postLikeRepository.existsByPostIdAndMemberId(unlikedPost.getId(), memberId)).isFalse();
            assertThat(postLikeRepository.countByPostId(unlikedPost.getId())).isEqualTo(0);
        }

        private void assertThatLikeNotExists(Long postId, Long memberId) {
            assertThat(postLikeRepository.existsByPostIdAndMemberId(postId, memberId)).isFalse();
            assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(0);
        }

        private void assertThatPostLiked(Post post, Long memberId) {
            assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
            assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1);
        }
    }

    // 헬퍼 메서드들
    private Post createTestPost() {
        Post post = postManager.create(createPostRequest(), AUTHOR_ID);
        flushAndClearContext();
        return postFinder.find(post.getId());
    }

    private Post createAndPublishPost() {
        Post post = createTestPost();
        postManager.publish(post.getId(), AUTHOR_ID);
        flushAndClearContext();
        return postFinder.find(post.getId());
    }

    private void flushAndClearContext() {
        entityManager.flush();
        entityManager.clear();
    }
}