package dooya.see.domain.post.event;

import dooya.see.domain.shared.DomainEvent;

public record CommentCreated(
        Long commentId,
        Long postId,
        Long memberId,
        Long parentCommentId,
        boolean isReply
) implements DomainEvent {
    public static CommentCreated of(Long commentId, Long postId, Long memberId, Long parentCommentId) {
        return new CommentCreated(commentId, postId, memberId, parentCommentId, parentCommentId != null);
    }

    public CommentCreated {
        isReply = (parentCommentId != null);
    }
}
