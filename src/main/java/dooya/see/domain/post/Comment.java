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

        comment.content = new CommentContent(request.body());
        comment.postId = requireNonNull(postId, "게시글 ID는 필수입니다");
        comment.memberId = requireNonNull(memberId, "회원 ID는 필수입니다");
        comment.parentCommentId = request.parentCommentId();
        comment.status = CommentStatus.ACTIVE;
        comment.metaData = CommentMetaData.create();

        comment.creationContext = new CommentCreationContext(
                postId, memberId, request.parentCommentId(), request.body()
        );

        return comment;
    }

    public void publishCreationEventIfNeeded() {
        if (creationContext != null && getId() != null) {
            addDomainEvent(CommentCreated.of(
                    getId(),
                    creationContext.postId(),
                    creationContext.memberId(),
                    creationContext.parentCommentId()
            ));
            creationContext = null;
        }
    }

    public void update(CommentUpdateRequest request) {
        validateCanBeModified();

        if (!request.hasUpdate()) {
            throw new EmptyCommentUpdateException();
        }

        String previousContent = this.content.text();
        this.content = new CommentContent(request.body());
        this.metaData = this.metaData.updateModifiedAt();

        addDomainEvent(new CommentUpdated(
                getId(),
                this.postId,
                this.memberId,
                previousContent,
                request.body()
        ));
    }

    public void delete() {
        validateCanBeModified();

        this.status = CommentStatus.DELETED;
        this.metaData = this.metaData.updateModifiedAt();

        addDomainEvent(new CommentDeleted(
                getId(),
                this.postId,
                this.memberId,
                isReply()
        ));
    }

    public void hide() {
        if (this.status == CommentStatus.HIDDEN) {
            throw InvalidCommentStatusException.alreadyHidden();
        }

        if (this.status == CommentStatus.DELETED) {
            throw InvalidCommentStatusException.cannotHideDeleted();
        }

        CommentStatus previousStatus = this.status;
        this.status = CommentStatus.HIDDEN;
        this.metaData = this.metaData.updateModifiedAt();

        addDomainEvent(new CommentHidden(
                getId(),
                this.postId,
                this.memberId,
                previousStatus
        ));
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

    private void validateCanBeModified() {
        if (!canBeModified()) {
            throw InvalidCommentStatusException.cannotModify();
        }
    }
}
