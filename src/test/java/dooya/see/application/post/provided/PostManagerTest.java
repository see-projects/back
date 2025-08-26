package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.PostLikeRepository;
import dooya.see.domain.post.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
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
record PostManagerTest(
    PostManager postManager, 
    EntityManager entityManager, 
    PostFinder postFinder, 
    ApplicationEventPublisher eventPublisher,
    PostLikeRepository postLikeRepository
) {

    @Test
    @DisplayName("게시글 생성 시 ID가 할당되고 초기 상태는 DRAFT가 된다")
    void create() {
        Post post = createPost();

        assertThat(post.getId()).isNotNull();
        assertThat(post.getMemberId()).isEqualTo(1L);
        assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(post.getMetaData()).isNotNull();
    }

    @Test
    @DisplayName("모든 필드를 수정하면 제목, 내용, 카테고리가 모두 변경된다")
    void updateAllFields() {
        Post post = createPost();

        postManager.update(updateAllFieldsRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo("수정된 제목");
        assertThat(post.getContent().body()).isEqualTo("수정된 내용");
        assertThat(post.getCategory()).isEqualTo(PostCategory.QNA);
    }

    @Test
    @DisplayName("제목만 수정하면 제목만 변경되고 내용과 카테고리는 기존 값을 유지한다")
    void updateTitleOnly() {
        Post post = createPost();

        String originalBody = post.getContent().body();
        PostCategory originalCategory = post.getCategory();

        postManager.update(updateTitleOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo("새로운 제목");
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(originalCategory);
    }

    @Test
    @DisplayName("내용만 수정하면 내용만 변경되고 제목과 카테고리는 기존 값을 유지한다")
    void updateBodyOnly() {
        Post post = createPost();
        String originalTitle = post.getContent().title();
        PostCategory originalCategory = post.getCategory();

        postManager.update(updateBodyOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo("새로운 내용");
        assertThat(post.getCategory()).isEqualTo(originalCategory);
    }

    @Test
    @DisplayName("카테고리만 수정하면 카테고리만 변경되고 제목과 내용은 기존 값을 유지한다")
    void updateCategoryOnly() {
        Post post = createPost();
        String originalTitle = post.getContent().title();
        String originalBody = post.getContent().body();

        postManager.update(updateCategoryOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.NOTICE);
    }

    @Test
    @DisplayName("변경사항이 없는 요청으로 수정 시 IllegalStateException이 발생한다")
    void updateWithNoChanges() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.update(noUpdateRequest(), post.getId(), 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정하려고 하면 PostNotFoundException이 발생한다")
    void updateNonExistentPost() {
        assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), 999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 수정 시도 시 UnauthorizedPostAccessException이 발생한다")
    void updateByNonOwner() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), post.getId(), 2L))
                .isInstanceOf(UnauthorizedPostAccessException.class);
    }

    @Test
    @DisplayName("게시글을 발행하면 PUBLISHED 상태가 되고 발행 시간이 기록된다")
    void publishDraftPost() {
        Post post = createPost();

        postManager.publish(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 발행된 게시글을 다시 발행하려고 하면 IllegalStateException이 발생한다")
    void publishAlreadyPublishedPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.publish(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("발행된 게시글을 숨김 처리하면 HIDDEN 상태로 변경된다")
    void hidePublishedPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        postManager.hide(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("HIDDEN 상태의 게시글을 발행하면 PUBLISHED 상태로 변경된다")
    void publishHiddenPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);
        postManager.hide(post.getId(), 1L);

        postManager.publish(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 숨김 처리된 게시글을 다시 숨기려고 하면 InvalidPostStatusTransitionException이 발생한다")
    void hideAlreadyHiddenPost() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);
        postManager.hide(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.hide(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("DRAFT 상태의 게시글을 숨김 처리하면 HIDDEN 상태로 변경된다")
    void hideDraftPost() {
        Post post = createPost();

        postManager.hide(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    @DisplayName("게시글을 삭제하면 DELETED 상태로 변경된다")
    void deletePost() {
        Post post = createPost();

        postManager.delete(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    @DisplayName("이미 삭제된 게시글을 다시 삭제하려고 하면 InvalidPostStatusTransitionException이 발생한다")
    void deleteAlreadyDeletedPost() {
        Post post = createPost();
        postManager.delete(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.delete(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    @DisplayName("게시글에 좋아요를 누르면 PostLike가 저장된다")
    void likePost() {
        Post post = createPost();
        Long memberId = 2L;
        
        Post result = postManager.likePost(post.getId(), memberId);
        
        assertThat(result.getId()).isEqualTo(post.getId());
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("게시글 좋아요를 취소하면 PostLike가 삭제된다")
    void unlikePost() {
        Post post = createPost();
        Long memberId = 3L;
        
        postManager.likePost(post.getId(), memberId);
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
        
        Post result = postManager.unlikePost(post.getId(), memberId);
        
        assertThat(result.getId()).isEqualTo(post.getId());
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isFalse();
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("동일 회원이 같은 게시글에 중복 좋아요를 누르면 멱등성이 보장된다")
    void likeDuplicatePost() {
        Post post = createPost();
        Long memberId = 4L;
        
        postManager.likePost(post.getId(), memberId);
        
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
        long firstLikeCount = postLikeRepository.countByPostId(post.getId());
        assertThat(firstLikeCount).isEqualTo(1);
        
        postManager.likePost(post.getId(), memberId);
        
        long secondLikeCount = postLikeRepository.countByPostId(post.getId());
        assertThat(secondLikeCount).isEqualTo(1);
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 좋아요를 취소하면 멱등성이 보장된다")
    void unlikeNonExistentLike() {
        Post post = createPost();
        Long memberId = 5L;
        
        postManager.unlikePost(post.getId(), memberId);
        
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isFalse();
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("여러 회원이 동일 게시글에 좋아요를 누를 수 있다")
    void likePostByMultipleMembers() {
        Post post = createPost();
        Long member1 = 10L;
        Long member2 = 20L;
        Long member3 = 30L;
        
        postManager.likePost(post.getId(), member1);
        postManager.likePost(post.getId(), member2);
        postManager.likePost(post.getId(), member3);
        
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(3);
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), member1)).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), member2)).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), member3)).isTrue();
    }

    @Test
    @DisplayName("좋아요 후 중복 좋아요를 시도해도 1개만 유지된다")
    void maintainSingleLikeAfterDuplicateAttempts() {
        Post post = createPost();
        Long memberId = 6L;
        
        postManager.likePost(post.getId(), memberId);
        long initialCount = postLikeRepository.countByPostId(post.getId());
        
        postManager.likePost(post.getId(), memberId);
        postManager.likePost(post.getId(), memberId);
        postManager.likePost(post.getId(), memberId);
        
        long finalCount = postLikeRepository.countByPostId(post.getId());
        assertThat(initialCount).isEqualTo(1);
        assertThat(finalCount).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 취소 후 다시 좋아요를 누를 수 있다")
    void likeAfterUnlike() {
        Post post = createPost();
        Long memberId = 7L;
        
        postManager.likePost(post.getId(), memberId);
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
        
        postManager.unlikePost(post.getId(), memberId);
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isFalse();
        
        postManager.likePost(post.getId(), memberId);
        
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요를 누르려고 하면 PostNotFoundException이 발생한다")
    void likeNonExistentPost() {
        assertThatThrownBy(() -> postManager.likePost(999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 좋아요를 취소하려고 하면 PostNotFoundException이 발생한다")
    void unlikeNonExistentPost() {
        assertThatThrownBy(() -> postManager.unlikePost(999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    private Post createPost() {
        Post post = postManager.create(createPostRequest(), 1L);
        entityManager.flush();
        entityManager.clear();

        return postFinder.find(post.getId());
    }
}
