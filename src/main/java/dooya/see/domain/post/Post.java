package dooya.see.domain.post;

import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;
import dooya.see.domain.post.event.*;
import dooya.see.domain.post.exception.InvalidPostStatusTransitionException;
import dooya.see.domain.post.exception.UnauthorizedPostAccessException;
import dooya.see.domain.shared.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

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

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    private List<Tag> tags = new ArrayList<>();

    public static Post create(PostCreateRequest request, Long memberId) {
        Post post = new Post();

        post.initializeBasicFields(request, memberId);
        post.determineInitialStatus(request);

        return post;
    }

    public void update(PostUpdateRequest request) {
        validateHasChanges(request);

        ContentUpdateResult contentResult = updateContentIfNeeded(request);
        boolean categoryChanged = updateCategoryIfNeeded(request);
        boolean tagsChanged = updateTagsIfNeeded(request);

        updateMetaData();

        publishUpdateEvent(requireNonNull(contentResult), categoryChanged, tagsChanged);
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
        validateCanInteract(memberId);

        publishLikeEvent(memberId);
    }

    public void unlike(Long memberId) {
        requireNonNull(memberId, "좋아요를 취소할 회원 ID는 필수입니다");
        validateCanInteract(memberId);

        publishUnlikeEvent(memberId);
    }

    public boolean isWrittenBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    // JPA 생명주기 콜백 - 생성 이벤트 발행
    @PostPersist
    private void publishCreationEvent() {
        this.addDomainEvent(new PostCreated(
                getId(),
                memberId,
                category,
                status == PostStatus.PUBLISHED,
                this
        ));
    }

    // Create 관련 메서드
    private void initializeBasicFields(PostCreateRequest request, Long memberId) {
        requireNonNull(request, "PostCreateRequest는 필수입니다");
        requireNonNull(request.title(), "제목은 필수입니다");
        requireNonNull(request.body(), "본문은 필수입니다");

        this.content = new PostContent(request.title(), request.body());
        this.memberId = requireNonNull(memberId, "작성자 ID는 필수입니다");
        this.category = requireNonNull(request.category(), "카테고리는 필수입니다");

        if (request.tags() != null && !request.tags().isEmpty()) {
            this.tags = request.tags().stream()
                    .map(Tag::new)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    private void determineInitialStatus(PostCreateRequest request) {
        this.status = request.publishImmediately() ? PostStatus.PUBLISHED : PostStatus.DRAFT;
        this.metaData = request.publishImmediately() ? PostMetaData.createPublished() : PostMetaData.create();
    }

    // Update 관련 메서드
    private static void validateHasChanges(PostUpdateRequest request) {
        if (!request.hasAnyUpdate()) {
            throw new IllegalStateException("변경사항이 없습니다");
        }
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

    private boolean updateTagsIfNeeded(PostUpdateRequest request) {
        if (request.tags().isEmpty())
            return false;

        List<Tag> newTags = request.tags().get().stream()
                .map(Tag::new)
                .collect(Collectors.toCollection(ArrayList::new));

        if (!(this.tags instanceof ArrayList))
            this.tags = new ArrayList<>(this.tags);

        boolean changed = !this.tags.equals(newTags);
        if (changed) {
            this.tags.clear();
            this.tags.addAll(newTags);
        }

        return changed;
    }

    private void updateMetaData() {
        this.metaData = this.metaData.updateModifiedAt();
    }

    private void publishUpdateEvent(ContentUpdateResult contentResult, boolean categoryChanged, boolean tagsChanged) {
        this.addDomainEvent(new PostUpdated(
                this.getId(),
                this.memberId,
                contentResult.titleChanged(),
                contentResult.bodyChanged(),
                categoryChanged,
                tagsChanged,
                this
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

    private void validateCanInteract(Long memberId) {
        if (!isWrittenBy(memberId) && status != PostStatus.PUBLISHED)
            throw new UnauthorizedPostAccessException("공개된 게시글만 상호작용할 수 있습니다");
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
