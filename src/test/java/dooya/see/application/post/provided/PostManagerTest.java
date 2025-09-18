package dooya.see.application.post.provided;

import dooya.see.SeeTestConfiguration;
import dooya.see.application.post.required.PostLikeRepository;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import dooya.see.domain.post.exception.PostNotFoundException;
import dooya.see.domain.post.exception.UnauthorizedPostAccessException;
import jakarta.persistence.EntityManager;
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
    @Test
    void 게시글_생성_시_ID가_할당되고_초기_상태는_DRAFT가_된다() {
        Post post = createPost();

        assertThat(post.getId()).isNotNull();
        assertThat(post.getMemberId()).isEqualTo(1L);
        assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(post.getMetaData()).isNotNull();
    }

    @Test
    void 모든_필드를_수정하면_제목_내용_카테고리가_모두_변경된다() {
        Post post = createPost();

        postManager.update(updateAllFieldsRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo("수정된 제목");
        assertThat(post.getContent().body()).isEqualTo("수정된 내용");
        assertThat(post.getCategory()).isEqualTo(PostCategory.QNA);
    }

    @Test
    void 제목만_수정하면_제목만_변경되고_내용과_카테고리는_기존_값을_유지한다() {
        Post post = createPost();

        String originalBody = post.getContent().body();
        PostCategory originalCategory = post.getCategory();

        postManager.update(updateTitleOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo("새로운 제목");
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(originalCategory);
    }

    @Test
    void 내용만_수정하면_내용만_변경되고_제목과_카테고리는_기존_값을_유지한다() {
        Post post = createPost();
        String originalTitle = post.getContent().title();
        PostCategory originalCategory = post.getCategory();

        postManager.update(updateBodyOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo("새로운 내용");
        assertThat(post.getCategory()).isEqualTo(originalCategory);
    }

    @Test
    void 카테고리만_수정하면_카테고리만_변경되고_제목과_내용은_기존_값을_유지한다() {
        Post post = createPost();
        String originalTitle = post.getContent().title();
        String originalBody = post.getContent().body();

        postManager.update(updateCategoryOnlyRequest(), post.getId(), 1L);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.NOTICE);
    }

    @Test
    void 변경사항이_없는_요청으로_수정_시_IllegalStateException이_발생한다() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.update(noUpdateRequest(), post.getId(), 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 존재하지_않는_게시글을_수정하려고_하면_PostNotFoundException이_발생한다() {
        assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), 999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void 작성자가_아닌_사용자가_게시글_수정_시도_시_UnauthorizedPostAccessException이_발생한다() {
        Post post = createPost();

        assertThatThrownBy(() -> postManager.update(updateAllFieldsRequest(), post.getId(), 2L))
                .isInstanceOf(UnauthorizedPostAccessException.class);
    }

    @Test
    void 게시글을_발행하면_PUBLISHED_상태가_되고_발행_시간이_기록된다() {
        Post post = createPost();

        postManager.publish(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    void 이미_발행된_게시글을_다시_발행하려고_하면_IllegalStateException이_발생한다() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.publish(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    void 발행된_게시글을_숨김_처리하면_HIDDEN_상태로_변경된다() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);

        postManager.hide(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    void HIDDEN_상태의_게시글을_발행하면_PUBLISHED_상태로_변경된다() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);
        postManager.hide(post.getId(), 1L);

        postManager.publish(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    void 이미_숨김_처리된_게시글을_다시_숨기려고_하면_InvalidPostStatusTransitionException이_발생한다() {
        Post post = createPost();
        postManager.publish(post.getId(), 1L);
        postManager.hide(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.hide(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    void DRAFT_상태의_게시글을_숨김_처리하면_HIDDEN_상태로_변경된다() {
        Post post = createPost();

        postManager.hide(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    void 게시글을_삭제하면_DELETED_상태로_변경된다() {
        Post post = createPost();

        postManager.delete(post.getId(), 1L);

        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    void 이미_삭제된_게시글을_다시_삭제하려고_하면_InvalidPostStatusTransitionException이_발생한다() {
        Post post = createPost();
        postManager.delete(post.getId(), 1L);

        assertThatThrownBy(() -> postManager.delete(post.getId(), 1L))
                .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    void 게시글에_좋아요를_누르면_PostLike가_저장된다() {
        Post post = createPost();
        Long memberId = 2L;
        
        Post result = postManager.likePost(post.getId(), memberId);
        
        assertThat(result.getId()).isEqualTo(post.getId());
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isTrue();
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1);
    }

    @Test
    void 게시글_좋아요를_취소하면_PostLike가_삭제된다() {
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
    void 동일_회원이_같은_게시글에_중복_좋아요를_누르면_멱등성이_보장된다() {
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
    void 존재하지_않는_좋아요를_취소하면_멱등성이_보장된다() {
        Post post = createPost();
        Long memberId = 5L;
        
        postManager.unlikePost(post.getId(), memberId);
        
        assertThat(postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId)).isFalse();
        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(0);
    }

    @Test
    void 여러_회원이_동일_게시글에_좋아요를_누를_수_있다() {
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
    void 좋아요_후_중복_좋아요를_시도해도_1개만_유지된다() {
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
    void 좋아요_취소_후_다시_좋아요를_누를_수_있다() {
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
    void 존재하지_않는_게시글에_좋아요를_누르려고_하면_PostNotFoundException이_발생한다() {
        assertThatThrownBy(() -> postManager.likePost(999L, 1L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void 존재하지_않는_게시글의_좋아요를_취소하려고_하면_PostNotFoundException이_발생한다() {
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
