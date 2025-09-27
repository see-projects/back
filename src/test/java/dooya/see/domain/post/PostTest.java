package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import dooya.see.domain.post.exception.UnauthorizedPostAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import dooya.see.domain.shared.DomainEvent;

import java.time.LocalDateTime;

import static dooya.see.domain.post.PostFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostTest {
    private static final Long AUTHOR_ID = 1L;
    private static final Long VIEWER_ID = 2L;
    private static final Long LIKER_ID = 3L;
    private static final Long POST_ID = 123L;

    private Post post;

    @BeforeEach
    void setUp() {
        post = Post.create(createPostRequest(), AUTHOR_ID);
    }

    @Nested
    class 게시글_생성 {
        @Test
        void 생성_시_모든_필드가_올바르게_설정되고_초기_상태는_DRAFT다() {
            assertThatPostCreatedWithCorrectState();
        }

        @Test
        void ID_할당_후_PostPersist_콜백으로_PostCreated_이벤트가_발생한다() {
            setIdAndSimulatePostPersist(POST_ID);

            assertThatPostCreatedEventOccurred(POST_ID, AUTHOR_ID, false);
        }

        @Test
        void 즉시_발행으로_생성_시_PostCreated_이벤트의_publishImmediately가_true다() {
            Post immediatePost = Post.create(createPostRequest(true), AUTHOR_ID);
            setIdAndSimulatePostPersist(immediatePost, POST_ID);

            PostCreated event = getFirstDomainEvent(immediatePost, PostCreated.class);
            assertThat(event.publishImmediately()).isTrue();
        }

        @Test
        void 생성만_하고_저장하지_않으면_아직_이벤트가_발생하지_않는다() {
            // Post.create()만 호출한 상태에서는 이벤트가 없어야 함
            assertThat(post.hasDomainEvents()).isFalse();
        }

        private void assertThatPostCreatedWithCorrectState() {
            assertThat(post.getContent().title()).isNotNull();
            assertThat(post.getMemberId()).isEqualTo(AUTHOR_ID);
            assertThat(post.getCategory()).isEqualTo(PostCategory.TECH);
            assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
            assertThat(post.getTags()).extracting("name")
                            .containsExactly("spring", "backend", "java");
            assertThatMetaDataInitialized();
        }

        private void assertThatMetaDataInitialized() {
            assertThat(post.getMetaData().createdAt()).isNotNull();
            assertThat(post.getMetaData().modifiedAt()).isNull();
            assertThat(post.getMetaData().publishedAt()).isNull();
        }

        private void assertThatPostCreatedEventOccurred(Long postId, Long memberId, boolean publishImmediately) {
            assertThat(post.hasDomainEvents()).isTrue();
            PostCreated event = getFirstDomainEvent(post, PostCreated.class);
            assertThat(event.postId()).isEqualTo(postId);
            assertThat(event.memberId()).isEqualTo(memberId);
            assertThat(event.publishImmediately()).isEqualTo(publishImmediately);
        }
    }

    @Nested
    class 게시글_수정 {
        @Test
        void 모든_필드_수정_시_모든_값이_변경되고_수정일시가_설정된다() {
            PostUpdateRequest request = updateAllFieldsRequest();

            post.update(request);

            assertThatAllFieldsUpdated();
            assertThatPostUpdatedEventOccurred(true, true, true, true);
        }

        @Test
        void 제목만_수정_시_제목만_변경되고_다른_필드는_유지된다() {
            String originalBody = post.getContent().body();
            PostCategory originalCategory = post.getCategory();
            PostUpdateRequest request = updateTitleOnlyRequest();

            post.update(request);

            assertThatOnlyTitleUpdated(originalBody, originalCategory);
            assertThatPostUpdatedEventOccurred(true, false, false, false);
        }

        @Test
        void 내용만_수정_시_내용만_변경되고_다른_필드는_유지된다() {
            String originalTitle = post.getContent().title();
            PostCategory originalCategory = post.getCategory();
            PostUpdateRequest request = updateBodyOnlyRequest();

            post.update(request);

            assertThatOnlyBodyUpdated(originalTitle, originalCategory);
        }

        @Test
        void 카테고리만_수정_시_카테고리만_변경되고_다른_필드는_유지된다() {
            String originalTitle = post.getContent().title();
            String originalBody = post.getContent().body();
            PostUpdateRequest request = updateCategoryOnlyRequest();

            post.update(request);

            assertThatOnlyCategoryUpdated(originalTitle, originalBody);
        }

        @Test
        void 복합_필드_수정_시_해당_필드들만_변경된다() {
            String originalBody = post.getContent().body();
            PostUpdateRequest request = updateTitleAndCategoryRequest();

            post.update(request);

            assertThatTitleAndCategoryUpdated(originalBody);
        }

        @Test
        void 변경사항이_없는_요청으로_수정_시_예외가_발생한다() {
            PostUpdateRequest request = noUpdateRequest();

            assertThatThrownBy(() -> post.update(request))
                    .isInstanceOf(IllegalStateException.class);
        }

        private void assertThatAllFieldsUpdated() {
            assertThat(post.getContent().title()).isEqualTo("수정된 제목");
            assertThat(post.getContent().body()).isEqualTo("수정된 내용");
            assertThat(post.getCategory()).isEqualTo(PostCategory.QNA);
            assertThat(post.getMetaData().modifiedAt()).isNotNull();
            assertThat(post.getTags()).extracting("name")
                    .containsExactly("spring", "springboot", "java");
        }

        private void assertThatOnlyTitleUpdated(String originalBody, PostCategory originalCategory) {
            assertThat(post.getContent().title()).isEqualTo("새로운 제목");
            assertThat(post.getContent().body()).isEqualTo(originalBody);
            assertThat(post.getCategory()).isEqualTo(originalCategory);
            assertThat(post.getMetaData().modifiedAt()).isNotNull();
        }

        private void assertThatOnlyBodyUpdated(String originalTitle, PostCategory originalCategory) {
            assertThat(post.getContent().title()).isEqualTo(originalTitle);
            assertThat(post.getContent().body()).isEqualTo("새로운 내용");
            assertThat(post.getCategory()).isEqualTo(originalCategory);
            assertThat(post.getMetaData().modifiedAt()).isNotNull();
        }

        private void assertThatOnlyCategoryUpdated(String originalTitle, String originalBody) {
            assertThat(post.getContent().title()).isEqualTo(originalTitle);
            assertThat(post.getContent().body()).isEqualTo(originalBody);
            assertThat(post.getCategory()).isEqualTo(PostCategory.NOTICE);
            assertThat(post.getMetaData().modifiedAt()).isNotNull();
        }

        private void assertThatTitleAndCategoryUpdated(String originalBody) {
            assertThat(post.getContent().title()).isEqualTo("제목과 카테고리 변경");
            assertThat(post.getContent().body()).isEqualTo(originalBody);
            assertThat(post.getCategory()).isEqualTo(PostCategory.GENERAL);
            assertThat(post.getMetaData().modifiedAt()).isNotNull();
        }

        private void assertThatPostUpdatedEventOccurred(boolean titleChanged, boolean bodyChanged, boolean categoryChanged, boolean tagsChanged) {
            PostUpdated event = getFirstDomainEvent(post, PostUpdated.class);
            assertThat(event.titleChanged()).isEqualTo(titleChanged);
            assertThat(event.bodyChanged()).isEqualTo(bodyChanged);
            assertThat(event.categoryChanged()).isEqualTo(categoryChanged);
            assertThat(event.tagsChanged()).isEqualTo(tagsChanged);
        }
    }

    @Nested
    class 게시글_상태_전이 {
        @Test
        void DRAFT에서_발행하면_PUBLISHED로_상태가_변경되고_발행일시가_설정된다() {
            post.publish();

            assertThatPostPublished();
            assertThatPostPublishedEventOccurred();
        }

        @Test
        void PUBLISHED에서_숨김_처리하면_HIDDEN으로_상태가_변경된다() {
            post.publish();
            clearDomainEvents();

            post.hide();

            assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
            assertThatPostHiddenEventOccurred(PostStatus.PUBLISHED);
        }

        @Test
        void HIDDEN에서_발행하면_PUBLISHED로_상태가_변경되고_발행일시가_갱신된다() {
            post.publish();
            LocalDateTime firstPublishedAt = post.getMetaData().publishedAt();
            post.hide();
            clearDomainEvents();

            post.publish();

            assertThatPostPublished();
            assertThat(post.getMetaData().publishedAt()).isAfter(firstPublishedAt);
        }

        @Test
        void DRAFT에서_숨김_처리하면_HIDDEN으로_상태가_변경된다() {
            clearDomainEvents();

            post.hide();

            assertThat(post.getStatus()).isEqualTo(PostStatus.HIDDEN);
            assertThatPostHiddenEventOccurred(PostStatus.DRAFT);
        }

        @Test
        void 게시글을_삭제하면_DELETED로_상태가_변경된다() {
            post.publish();
            clearDomainEvents();

            post.delete();

            assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
            assertThatPostDeletedEventOccurred(PostStatus.PUBLISHED);
        }

        private void assertThatPostPublished() {
            assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(post.getMetaData().publishedAt()).isNotNull();
        }

        private void assertThatPostPublishedEventOccurred() {
            PostPublished event = getFirstDomainEvent(post, PostPublished.class);
            assertThat(event.postId()).isEqualTo(post.getId());
            assertThat(event.memberId()).isEqualTo(AUTHOR_ID);
        }

        private void assertThatPostHiddenEventOccurred(PostStatus previousStatus) {
            PostHidden event = getFirstDomainEvent(post, PostHidden.class);
            assertThat(event.postId()).isEqualTo(post.getId());
            assertThat(event.memberId()).isEqualTo(AUTHOR_ID);
            assertThat(event.previousStatus()).isEqualTo(previousStatus);
        }

        private void assertThatPostDeletedEventOccurred(PostStatus previousStatus) {
            PostDeleted event = getFirstDomainEvent(post, PostDeleted.class);
            assertThat(event.postId()).isEqualTo(post.getId());
            assertThat(event.memberId()).isEqualTo(AUTHOR_ID);
            assertThat(event.previousStatus()).isEqualTo(previousStatus);
        }
    }

    @Nested
    class 잘못된_상태_전이 {
        @Test
        void PUBLISHED_상태에서_다시_발행하면_예외가_발생한다() {
            post.publish();

            assertThatThrownBy(() -> post.publish())
                    .isInstanceOf(InvalidPostStatusTransitionException.class);
        }

        @Test
        void HIDDEN_상태에서_다시_숨기면_예외가_발생한다() {
            post.publish();
            post.hide();

            assertThatThrownBy(() -> post.hide())
                    .isInstanceOf(InvalidPostStatusTransitionException.class);
        }

        @Test
        void 이미_삭제된_게시글을_다시_삭제하면_예외가_발생한다() {
            post.delete();

            assertThatThrownBy(() -> post.delete())
                    .isInstanceOf(InvalidPostStatusTransitionException.class);
        }
    }

    @Nested
    class 게시글_소유권 {
        @Test
        void 작성자_확인_시_올바른_결과를_반환한다() {
            assertThat(post.isWrittenBy(AUTHOR_ID)).isTrue();
            assertThat(post.isWrittenBy(VIEWER_ID)).isFalse();
        }
    }

    @Nested
    class 게시글_상호작용 {
        @Test
        void 조회_시_PostViewed_이벤트가_발생한다() {
            setIdAndClearEvents(POST_ID);

            post.view(VIEWER_ID);

            assertThatPostViewedEventOccurred(POST_ID, VIEWER_ID);
        }

        @Test
        void 익명_사용자_조회_시_PostViewed_이벤트의_memberId가_null이다() {
            setIdAndClearEvents(POST_ID);

            post.view(null);

            assertThatPostViewedEventOccurred(POST_ID, null);
        }

        @Test
        void 좋아요_시_PostLiked_이벤트가_발생한다() {
            post.publish();
            setIdAndClearEvents(POST_ID);

            post.like(LIKER_ID);

            assertThatPostLikedEventOccurred(POST_ID, LIKER_ID);
        }

        @Test
        void 좋아요_취소_시_PostUnliked_이벤트가_발생한다() {
            post.publish();
            setIdAndClearEvents(POST_ID);

            post.unlike(LIKER_ID);

            assertThatPostUnlikedEventOccurred(POST_ID, LIKER_ID);
        }

        @Test
        void 작성자는_비공개_게시글에도_좋아요할_수_있다() {
            post.like(AUTHOR_ID);
        }

        @Test
        void 비공개_게시글에_타인이_좋아요하면_예외가_발생한다() {
            assertThatThrownBy(() -> post.like(LIKER_ID))
                .isInstanceOf(UnauthorizedPostAccessException.class)
                    .hasMessageContaining("공개된 게시글만 상호작용할 수 있습니다");
        }

        @Test
        void 좋아요할_회원_ID가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> post.like(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("좋아요를 누를 회원 ID는 필수입니다");
        }

        @Test
        void 좋아요_취소할_회원_ID가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> post.unlike(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("좋아요를 취소할 회원 ID는 필수입니다");
        }

        private void assertThatPostViewedEventOccurred(Long postId, Long viewerId) {
            PostViewed event = getFirstDomainEvent(post, PostViewed.class);
            assertThat(event.postId()).isEqualTo(postId);
            assertThat(event.memberId()).isEqualTo(viewerId);
        }

        private void assertThatPostLikedEventOccurred(Long postId, Long likerId) {
            PostLiked event = getFirstDomainEvent(post, PostLiked.class);
            assertThat(event.postId()).isEqualTo(postId);
            assertThat(event.memberId()).isEqualTo(likerId);
        }

        private void assertThatPostUnlikedEventOccurred(Long postId, Long unlikerId) {
            PostUnliked event = getFirstDomainEvent(post, PostUnliked.class);
            assertThat(event.postId()).isEqualTo(postId);
            assertThat(event.memberId()).isEqualTo(unlikerId);
        }
    }

    @Nested
    class 도메인_이벤트_관리 {
        @Test
        void 이벤트를_클리어하면_이벤트_목록이_비워진다() {
            setIdAndSimulatePostPersist(POST_ID);
            assertThat(post.hasDomainEvents()).isTrue();

            post.clearDomainEvents();

            assertThat(post.hasDomainEvents()).isFalse();
            assertThat(post.getDomainEvents()).isEmpty();
        }
    }

    // 헬퍼 메서드들 - @PostPersist 방식에 맞게 수정
    private void clearDomainEvents() {
        post.clearDomainEvents();
    }

    private void setIdAndClearEvents(Long id) {
        setPostId(post, id);
        clearDomainEvents();
    }

    /**
     * ID 설정 후 @PostPersist 콜백을 시뮬레이션
     * JPA 저장 시 일어나는 상황을 테스트에서 재현
     */
    private void setIdAndSimulatePostPersist(Long id) {
        setIdAndSimulatePostPersist(post, id);
    }

    private void setIdAndSimulatePostPersist(Post targetPost, Long id) {
        setPostId(targetPost, id);
        // @PostPersist 메서드를 직접 호출하여 시뮬레이션
        ReflectionTestUtils.invokeMethod(targetPost, "publishCreationEvent");
    }

    private <T extends DomainEvent> T getFirstDomainEvent(Post targetPost, Class<T> eventType) {
        assertThat(targetPost.hasDomainEvents()).isTrue();
        assertThat(targetPost.getDomainEvents()).hasSize(1);

        DomainEvent event = targetPost.getDomainEvents().getFirst();
        assertThat(event).isInstanceOf(eventType);

        return eventType.cast(event);
    }

    private void setPostId(Post targetPost, Long id) {
        ReflectionTestUtils.setField(targetPost, "id", id);
    }
}