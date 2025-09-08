package dooya.see.domain.post;

import dooya.see.domain.post.event.*;
import dooya.see.domain.shared.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends AbstractAggregateRoot {
    @Embedded
    private CommentContent content;

    private Long postId;

    private Long memberId;

    private Long parentCommentId;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @Embedded
    private CommentMetaData metaData;

    @Transient
    private CommentCreationContext creationContext;

    private record CommentCreationContext(
            Long postId, 
            Long memberId, 
            Long parentCommentId,
            String content
    ) {}

    public static Comment create(CommentCreateRequest request, Long postId, Long memberId) {
        Comment comment = new Comment();

        comment.initializeComment(request, postId, memberId);
        comment.prepareCreationEvent(request);

        return comment;
    }

    public void publishCreationEventIfNeeded() {
        if (hasCreationContext() && getId() != null) {
            publishCreationEvent();
            clearCreationContext();
        }
    }

    public void update(CommentUpdateRequest request) {
        validateCanBeModified();
        validateUpdateRequest(request);

        String previousContent = updateContent(request);
        updateModificationMetadata();
        publishUpdateEvent(previousContent, request.body());
    }

    public void delete() {
        validateCanBeModified();
        
        changeStatusToDeleted();
        updateModificationMetadata();
        publishDeleteEvent();
    }

    public void hide() {
        validateCanHide();
        
        CommentStatus previousStatus = changeStatusToHidden();
        updateModificationMetadata();
        publishHideEvent(previousStatus);
    }

    public boolean isReply() {
        return parentCommentId != null;
    }

    public boolean isTopLevel() {
        return parentCommentId == null;
    }

    public boolean canBeModified() {
        return status == CommentStatus.ACTIVE;
    }

    public boolean isDelete() {
        return status == CommentStatus.DELETED;
    }

    public boolean isHidden() {
        return status == CommentStatus.HIDDEN;
    }

    public boolean isWrittenBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    private void initializeComment(CommentCreateRequest request, Long postId, Long memberId) {
        this.content = new CommentContent(request.body());
        this.postId = requireNonNull(postId, "게시글 ID는 필수입니다");
        this.memberId = requireNonNull(memberId, "회원 ID는 필수입니다");
        this.parentCommentId = request.parentCommentId();
        this.status = CommentStatus.ACTIVE;
        this.metaData = CommentMetaData.create();
    }

    private void prepareCreationEvent(CommentCreateRequest request) {
        this.creationContext = new CommentCreationContext(
                this.postId, this.memberId, request.parentCommentId(), request.body()
        );
    }

    private boolean hasCreationContext() {
        return creationContext != null;
    }

    private void publishCreationEvent() {
        addDomainEvent(CommentCreated.of(
                getId(),
                creationContext.postId(),
                creationContext.memberId(),
                creationContext.parentCommentId()
        ));
    }

    private void clearCreationContext() {
        creationContext = null;
    }

    private void validateUpdateRequest(CommentUpdateRequest request) {
        if (!request.hasUpdate()) {
            throw new EmptyCommentUpdateException();
        }
    }

    private String updateContent(CommentUpdateRequest request) {
        String previousContent = this.content.text();
        this.content = new CommentContent(request.body());
        return previousContent;
    }

    private void updateModificationMetadata() {
        this.metaData = this.metaData.updateModifiedAt();
    }

    private void publishUpdateEvent(String previousContent, String newContent) {
        addDomainEvent(new CommentUpdated(
                getId(),
                this.postId,
                this.memberId,
                previousContent,
                newContent
        ));
    }

    private void changeStatusToDeleted() {
        this.status = CommentStatus.DELETED;
    }

    private void publishDeleteEvent() {
        addDomainEvent(new CommentDeleted(
                getId(),
                this.postId,
                this.memberId,
                isReply()
        ));
    }

    private void validateCanHide() {
        validateNotAlreadyHidden();
        validateNotDeleted();
    }

    private void validateNotAlreadyHidden() {
        if (this.status == CommentStatus.HIDDEN) {
            throw InvalidCommentStatusException.alreadyHidden();
        }
    }

    private void validateNotDeleted() {
        if (this.status == CommentStatus.DELETED) {
            throw InvalidCommentStatusException.cannotHideDeleted();
        }
    }

    private CommentStatus changeStatusToHidden() {
        CommentStatus previousStatus = this.status;
        this.status = CommentStatus.HIDDEN;
        return previousStatus;
    }

    private void publishHideEvent(CommentStatus previousStatus) {
        addDomainEvent(new CommentHidden(
                getId(),
                this.postId,
                this.memberId,
                previousStatus
        ));
    }

    private void validateCanBeModified() {
        if (!canBeModified()) {
            throw InvalidCommentStatusException.cannotModify();
        }
    }
}
