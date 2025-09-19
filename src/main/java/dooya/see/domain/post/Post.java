package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import dooya.see.domain.shared.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.state;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends AbstractAggregateRoot {
    @Embedded
    private PostContent content;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Embedded
    private PostMetaData metaData;

    @Transient
    private PostCreationContext creationContext;

    private record PostCreationContext(boolean publishImmediately) {}

    public static Post create(PostCreateRequest request, Long memberId) {
        Post post = new Post();

        post.initializeBasicFields(request, memberId);
        post.determineInitialStatus(request);
        post.setupCreationContext(request);

        return post;
    }

    public void publishCreationEventIfNeeded() {
        publishCreationEvent();
    }

    public void update(PostUpdateRequest request) {
        validateHasChanges(request);

        ContentUpdateResult contentResult = updateContentIfNeeded(request);
        boolean categoryChanged = updateCategoryIfNeeded(request);

        updateMetaData();

        publishUpdateEvent(requireNonNull(contentResult), categoryChanged);
    }

    public void publish() {
        validateCanPublish();

        this.status = PostStatus.PUBLISHED;
        this.metaData = this.metaData.updatePublishedAt();

        this.addDomainEvent(new PostPublished(this.getId(), this.memberId));
    }

    public void hide() {
        validateCanHide();

        PostStatus previousStatus = changeStatusToHidden();
        publishHideEvent(previousStatus);
    }

    public void delete() {
        validateCanDelete();

        PostStatus previousStatus = changeStatusToDeleted();
        publishDeleteEvent(previousStatus);
    }

    public void view(Long viewerId) {
        publishViewEvent(viewerId);
    }

    public void like(Long memberId) {
        requireNonNull(memberId, "좋아요를 누를 회원 ID는 필수입니다");

        publishLikeEvent(memberId);
    }

    public void unlike(Long memberId) {
        requireNonNull(memberId, "좋아요를 취소할 회원 ID는 필수입니다");

        publishUnlikeEvent(memberId);
    }

    public boolean isWrittenBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    // Create 관련 메서드
    private void initializeBasicFields(PostCreateRequest request, Long memberId) {
        requireNonNull(request, "PostCreateRequest는 필수입니다");
        requireNonNull(request.title(), "제목은 필수입니다");
        requireNonNull(request.body(), "본문은 필수입니다");

        this.content = new PostContent(request.title(), request.body());
        this.memberId = requireNonNull(memberId, "작성자 ID는 필수입니다");
        this.category = requireNonNull(request.category(), "카테고리는 필수입니다");
    }

    private void determineInitialStatus(PostCreateRequest request) {
        this.status = request.publishImmediately() ? PostStatus.PUBLISHED : PostStatus.DRAFT;
        this.metaData = request.publishImmediately() ? PostMetaData.createPublished() : PostMetaData.create();
    }

    private void setupCreationContext(PostCreateRequest request) {
        this.creationContext = new PostCreationContext(request.publishImmediately());
    }

    // Creation Event 관련 메서드 - 중복 방지 로직 강화
    private void publishCreationEvent() {
        if (creationContext != null && getId() != null) {
            // 이미 발행된 PostCreated 이벤트가 있는지 확인
            boolean alreadyPublished = getDomainEvents().stream()
                    .anyMatch(event -> event instanceof PostCreated created &&
                            created.postId().equals(getId()));

            if (!alreadyPublished) {
                addDomainEvent(new PostCreated(
                        getId(), memberId, category, creationContext.publishImmediately()
                ));
            }
            creationContext = null;
        }
    }

    // Update 관련 메서드
    private static void validateHasChanges(PostUpdateRequest request) {
        state(request.hasAnyUpdate(), "변경사항이 없습니다");
    }

    private ContentUpdateResult updateContentIfNeeded(PostUpdateRequest request) {
        if (!hasContentToUpdate(request))
            return new ContentUpdateResult(false, false);

        String originalTitle = this.content.title();
        String originalBody = this.content.body();

        String newTitle = request.title().orElse(originalTitle);
        String newBody = request.body().orElse(originalBody);

        boolean titleChanged = !originalTitle.equals(newTitle);
        boolean bodyChanged = !originalBody.equals(newBody);

        this.content = new PostContent(newTitle, newBody);

        return new ContentUpdateResult(titleChanged, bodyChanged);
    }

    private boolean updateCategoryIfNeeded(PostUpdateRequest request) {
        if (!hasCategoryToUpdate(request))
            return false;
        PostCategory originalCategory = this.category;
        this.category = request.category().get();
        return !originalCategory.equals(this.category);
    }

    private void updateMetaData() {
        this.metaData = this.metaData.updateModifiedAt();
    }

    private void publishUpdateEvent(ContentUpdateResult contentResult, boolean categoryChanged) {
        this.addDomainEvent(new PostUpdated(
                this.getId(),
                this.memberId,
                contentResult.titleChanged(),
                contentResult.bodyChanged(),
                categoryChanged
        ));
    }

    // Status Transition 관련 메서드
    private void validateCanPublish() {
        if (this.status != PostStatus.DRAFT && this.status != PostStatus.HIDDEN) {
            throw new InvalidPostStatusTransitionException(this.status, "발행");
        }
    }

    private void validateCanHide() {
        validateStatusNotEquals(PostStatus.HIDDEN, "숨김");
        validateStatusNotEquals(PostStatus.DELETED, "숨김");
    }

    private void validateCanDelete() {
        validateStatusNotEquals(PostStatus.DELETED, "삭제");
    }

    private void validateStatusNotEquals(PostStatus prohibitedStatus, String operation) {
        if (this.status == prohibitedStatus) {
            throw new InvalidPostStatusTransitionException(this.status, operation);
        }
    }

    private PostStatus changeStatusToHidden() {
        PostStatus previousStatus = this.status;
        this.status = PostStatus.HIDDEN;
        return previousStatus;
    }

    private PostStatus changeStatusToDeleted() {
        PostStatus previousStatus = this.status;
        this.status = PostStatus.DELETED;
        return previousStatus;
    }

    // Event Publishing 관련 메서드
    private void publishHideEvent(PostStatus previousStatus) {
        this.addDomainEvent(new PostHidden(this.getId(), this.memberId, previousStatus));
    }

    private void publishDeleteEvent(PostStatus previousStatus) {
        this.addDomainEvent(new PostDeleted(this.getId(), this.memberId, previousStatus));
    }

    private void publishViewEvent(Long viewerId) {
        if (this.getId() != null) {
            this.addDomainEvent(new PostViewed(this.getId(), viewerId));
        }
    }

    private void publishLikeEvent(Long memberId) {
        if (this.getId() != null) {
            this.addDomainEvent(new PostLiked(this.getId(), memberId));
        }
    }

    private void publishUnlikeEvent(Long memberId) {
        if (this.getId() != null) {
            this.addDomainEvent(new PostUnliked(this.getId(), memberId));
        }
    }

    // 유틸리티 Static 메서드
    private static boolean hasCategoryToUpdate(PostUpdateRequest request) {
        return request.category().isPresent();
    }

    private static boolean hasContentToUpdate(PostUpdateRequest request) {
        return request.title().isPresent() || request.body().isPresent();
    }
}