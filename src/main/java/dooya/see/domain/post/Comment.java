package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

import java.util.Objects;

@Entity
@Getter
public class Comment extends AbstractEntity {
    @Embedded
    private CommentContent content;

    private Long postId;

    private Long memberId;

    private Long parentCommentId;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @Embedded
    private CommentMetaData metaData;

    public static Comment create(CommentCreateRequest request, Long postId, Long memberId) {
        Comment comment = new Comment();

        comment.content = new CommentContent(request.body());
        comment.postId = Objects.requireNonNull(postId);
        comment.memberId = Objects.requireNonNull(memberId);
        comment.parentCommentId = request.parentCommentId();
        comment.status = CommentStatus.ACTIVE;
        comment.metaData = CommentMetaData.create();

        return comment;
    }

    public void update(CommentUpdateRequest request) {
        validateCanBeModified();

        if (!request.hasUpdate()) {
            throw new IllegalArgumentException("수정할 내용이 없습니다");
        }

        this.content = new CommentContent(request.body());
        this.metaData = this.metaData.updateModifiedAt();
    }

    public void delete() {
        validateCanBeModified();

        CommentStatus previousStatus = this.status;
        this.status = CommentStatus.DELETED;
        this.metaData = this.metaData.updateModifiedAt();
    }

    public void hide() {
        if (this.status == CommentStatus.HIDDEN) {
            throw new IllegalStateException("이미 숨김 처리된 댓글입니다");
        }

        if (this.status == CommentStatus.DELETED) {
            throw new IllegalStateException("삭제된 댓글을 숨김 처리할 수 없습니다");
        }

        this.status = CommentStatus.HIDDEN;
        this.metaData = this.metaData.updateModifiedAt();
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
            throw new IllegalStateException("수정할수 없는 댓글입니다");
        }
    }
}
