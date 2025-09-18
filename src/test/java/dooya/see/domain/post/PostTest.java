package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import dooya.see.domain.shared.DomainEvent;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostTest {
    Post post;

    @BeforeEach
    void setUp() {
        post = Post.create(createPostRequest(), 1L);
    }

    @Test
    void Post_생성_시_요정_정보와_작성자_ID가_올바르게_설정되고_초기_상태는_DRAFT가_된다() {
        assertThat(post.getContent().title()).isNotNull();
        assertThat(post.getMemberId()).isEqualTo(1L);
        assertThat(post.getCategory()).isEqualTo(PostCategory.TECH);
        assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
        assertThat(post.getMetaData().createdAt()).isNotNull();
        assertThat(post.getMetaData().modifiedAt()).isNull();
        assertThat(post.getMetaData().publishedAt()).isNull();
    }

    @Test
    void 모든_필드를_업데이트하면_제목_내용_카테고리가_모두_변경되고_수정일시가_설정된다() {
        PostUpdateRequest request = updateAllFieldsRequest();

        post.update(request);

        assertThat(post.getContent().title()).isEqualTo("수정된 제목");
        assertThat(post.getContent().body()).isEqualTo("수정된 내용");
        assertThat(post.getCategory()).isEqualTo(PostCategory.QNA);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    void 제목만_업데이트하면_제목만_변경되고_내용과_카테고리는_기존_값을_유지한다() {
        String originalBody = post.getContent().body();
        PostCategory originalCategory = post.getCategory();

        PostUpdateRequest request = updateTitleOnlyRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo("새로운 제목");
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(originalCategory);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    void 내용만_업데이트하면_내용만_변경되고_제목과_카테고리는_기존_값을_유지한다() {
        String originalTitle = post.getContent().title();
        PostCategory originalCategory = post.getCategory();

        PostUpdateRequest request = updateBodyOnlyRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo("새로운 내용");
        assertThat(post.getCategory()).isEqualTo(originalCategory);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    void 카테고리만_업데이트하면_카테고리만_변경되고_제목과_내용은_기존_값을_유지한다() {
        String originalTitle = post.getContent().title();
        String originalBody = post.getContent().body();

        PostUpdateRequest request = updateCategoryOnlyRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo(originalTitle);
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.NOTICE);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    void 제목과_카테고리를_함께_업데이트하면_해당_필드들만_변경되고_내용은_기존_값을_유지한다() {
        String originalBody = post.getContent().body();

        PostUpdateRequest request = updateTitleAndCategoryRequest();
        post.update(request);

        assertThat(post.getContent().title()).isEqualTo("제목과 카테고리 변경");
        assertThat(post.getContent().body()).isEqualTo(originalBody);
        assertThat(post.getCategory()).isEqualTo(PostCategory.GENERAL);
        assertThat(post.getMetaData().modifiedAt()).isNotNull();
    }

    @Test
    void 변경하상이_없는_요청으로_업데이트_시_IllegalStateException이_발생한다() {
        PostUpdateRequest request = noUpdateRequest();

        assertThatThrownBy(() -> post.update(request))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void DRAFT_상태의_게시글을_발행하면_상태가_PUBLISHED로_변경되고_발행일시가_설정된다() {
        post.publish();

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    void PUBLISHED_상태의_게시글을_숨김_처리하면_상태가_HIDDEN으로_변경된다() {
        post.publish();
        post.hide();

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    void HIDDEN_상태의_게시글을_발행하면_상태가_PUBLISHED로_변경되고_발행일시가_갱신된다() {
        post.publish();
        post.hide();
        
        post.publish();

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getMetaData().publishedAt()).isNotNull();
    }

    @Test
    void DRAFT_상태의_게시글을_숨김_처리하면_상태가_HIDDEN으로_변경된다() {
        post.hide();

        assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
    }

    @Test
    void PUBLISHED_상태의_게시글을_다시_발행하려고_하면_InvalidPostStatusTransitionException이_발생한다() {
        post.publish();
        
        assertThatThrownBy(() -> post.publish())
            .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    void HIDDEN_상태의_게시글을_다시_숨기려고_하면_InvalidPostStatusTransitionException이_발생한다() {
        post.publish();
        post.hide();
        
        assertThatThrownBy(() -> post.hide())
            .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    void 게시글을_삭제하면_상태가_DELETED로_변경된다() {
        post.delete();

        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
    }

    @Test
    void 이미_삭제된_게시글을_다시_삭제하려고_하면_InvalidPostStatusTransitionException이_발생한다() {
        post.delete();

        assertThatThrownBy(() -> post.delete())
            .isInstanceOf(InvalidPostStatusTransitionException.class);
    }

    @Test
    void 게시글_작성자_확인_시_올바른_memberId면_true_다른_memberId면_false를_반환한다() {
        assertThat(post.isWrittenBy(1L)).isTrue();
        assertThat(post.isWrittenBy(2L)).isFalse();
    }

    @Test
    void Post_생성_후_ID_할당하고_이벤트_발행하면_PostCreated_도메인_이벤트가_발생한다() {
        PostCreateRequest request = createPostRequest();
        Long memberId = 1L;

        Post post = Post.create(request, memberId);
        
        // 생성 직후에는 이벤트가 없어야 함
        assertThat(post.hasDomainEvents()).isFalse();
        
        // ID를 시뮬레이션으로 할당 (실제로는 JPA가 할당)
        setPostId(post, 123L);
        
        // 이벤트 발행
        post.publishCreationEventIfNeeded();

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostCreated.class);
        
        PostCreated postCreated = (PostCreated) event;
        assertThat(postCreated.postId()).isEqualTo(123L);
        assertThat(postCreated.memberId()).isEqualTo(memberId);
        assertThat(postCreated.category()).isEqualTo(request.category());
        assertThat(postCreated.publishImmediately()).isEqualTo(request.publishImmediately());
    }

    @Test
    void 즉시_발행으로_Post_생성_후_ID_할당하고_이벤트_발행하면_PostCreated_이벤트의_publishImmediately가_true다() {
        PostCreateRequest request = createPostRequest(true);
        Long memberId = 1L;

        Post post = Post.create(request, memberId);
        
        // ID를 시뮬레이션으로 할당
        setPostId(post, 456L);
        
        // 이벤트 발행
        post.publishCreationEventIfNeeded();

        PostCreated event = (PostCreated) post.getDomainEvents().getFirst();
        assertThat(event.publishImmediately()).isTrue();
    }

    @Test
    void 게시글_발행_시_PostPublished_도메인_이벤트가_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        post.clearDomainEvents();

        post.publish();

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostPublished.class);
        
        PostPublished postPublished = (PostPublished) event;
        assertThat(postPublished.postId()).isEqualTo(post.getId());
        assertThat(postPublished.memberId()).isEqualTo(1L);
    }

    @Test
    void 도메인_이벤트를_클리어하면_이벤트_목록이_비워진다() {
        Post post = Post.create(createPostRequest(), 1L);
        
        // ID 할당 후 이벤트 발행
        setPostId(post, 789L);
        post.publishCreationEventIfNeeded();
        
        assertThat(post.hasDomainEvents()).isTrue();

        post.clearDomainEvents();

        assertThat(post.hasDomainEvents()).isFalse();
        assertThat(post.getDomainEvents()).isEmpty();
    }

    @Test
    void 게시글_수정_시_PostUpdated_도메인_이벤트가_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        post.clearDomainEvents();
        PostUpdateRequest request = updateAllFieldsRequest();

        post.update(request);

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostUpdated.class);
        
        PostUpdated postUpdated = (PostUpdated) event;
        assertThat(postUpdated.postId()).isEqualTo(post.getId());
        assertThat(postUpdated.memberId()).isEqualTo(1L);
        assertThat(postUpdated.titleChanged()).isTrue();
        assertThat(postUpdated.bodyChanged()).isTrue();
        assertThat(postUpdated.categoryChanged()).isTrue();
    }

    @Test
    void 제목만_수정_시_PostUpdated_이벤트에서_titleChanged만_true다() {
        Post post = Post.create(createPostRequest(), 1L);
        post.clearDomainEvents();
        PostUpdateRequest request = updateTitleOnlyRequest();

        post.update(request);

        PostUpdated event = (PostUpdated) post.getDomainEvents().getFirst();
        assertThat(event.titleChanged()).isTrue();
        assertThat(event.bodyChanged()).isFalse();
        assertThat(event.categoryChanged()).isFalse();
    }

    @Test
    void 게시글_숨김_시_PostHidden_도메인_이벤트가_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        post.publish();
        post.clearDomainEvents();

        post.hide();

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostHidden.class);
        
        PostHidden postHidden = (PostHidden) event;
        assertThat(postHidden.postId()).isEqualTo(post.getId());
        assertThat(postHidden.memberId()).isEqualTo(1L);
        assertThat(postHidden.previousStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void 게시글_삭제_시_PostDeleted_도메인_이벤트가_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        post.publish();
        post.clearDomainEvents();

        post.delete();

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostDeleted.class);
        
        PostDeleted postDeleted = (PostDeleted) event;
        assertThat(postDeleted.postId()).isEqualTo(post.getId());
        assertThat(postDeleted.memberId()).isEqualTo(1L);
        assertThat(postDeleted.previousStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void DRAFT_상태에서_숨김_처리_시_이전_상태가_DRAFT로_기록된다() {
        Post post = Post.create(createPostRequest(), 1L);
        post.clearDomainEvents();

        post.hide();

        PostHidden event = (PostHidden) post.getDomainEvents().getFirst();
        assertThat(event.previousStatus()).isEqualTo(PostStatus.DRAFT);
    }

    @Test
    void 게시글_조회_시_PostViewed_도메인_이벤트가_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        setPostId(post, 100L);
        post.clearDomainEvents();

        post.view(2L);  // 다른 사용자가 조회

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostViewed.class);
        
        PostViewed postViewed = (PostViewed) event;
        assertThat(postViewed.postId()).isEqualTo(100L);
        assertThat(postViewed.memberId()).isEqualTo(2L);
    }

    @Test
    void 익명_사용자가_게시글_조회_시_PostViewed_이벤트의_memberId가_null이다() {
        Post post = Post.create(createPostRequest(), 1L);
        setPostId(post, 100L);
        post.clearDomainEvents();

        post.view(null);  // 익명 사용자 조회

        PostViewed event = (PostViewed) post.getDomainEvents().getFirst();
        assertThat(event.postId()).isEqualTo(100L);
        assertThat(event.memberId()).isNull();
    }

    @Test
    void 게시글_좋아요_시_PostLiked_도메인_이벤트가_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        setPostId(post, 200L);
        post.clearDomainEvents();

        post.like(3L);

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostLiked.class);
        
        PostLiked postLiked = (PostLiked) event;
        assertThat(postLiked.postId()).isEqualTo(200L);
        assertThat(postLiked.memberId()).isEqualTo(3L);
    }

    @Test
    void 게시글_좋아요_취소_시_PostUnliked_도메인_이벤트가_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        setPostId(post, 300L);
        post.clearDomainEvents();

        post.unlike(4L);

        assertThat(post.hasDomainEvents()).isTrue();
        assertThat(post.getDomainEvents()).hasSize(1);
        
        DomainEvent event = post.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(PostUnliked.class);
        
        PostUnliked postUnliked = (PostUnliked) event;
        assertThat(postUnliked.postId()).isEqualTo(300L);
        assertThat(postUnliked.memberId()).isEqualTo(4L);
    }

    @Test
    void 좋아요할_회원_ID가_null이면_IllegalArgumentException이_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        setPostId(post, 400L);

        assertThatThrownBy(() -> post.like(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("좋아요를 누를 회원 ID는 필수입니다");
    }

    @Test
    void 좋아요_취소할_회원_ID가_null이면_IllegalArgumentException이_발생한다() {
        Post post = Post.create(createPostRequest(), 1L);
        setPostId(post, 500L);

        assertThatThrownBy(() -> post.unlike(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("좋아요를 취소할 회원 ID는 필수입니다");
    }
    
    /**
     * 테스트용 헬퍼 메서드: Post 엔티티에 ID를 설정
     */
    private void setPostId(Post post, Long id) {
        ReflectionTestUtils.setField(post, "id", id);
    }
}
