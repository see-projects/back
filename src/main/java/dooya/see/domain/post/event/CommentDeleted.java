package dooya.see.domain.post.event;

import dooya.see.domain.shared.DomainEvent;

public record CommentDeleted(
        Long commentId,
        Long postId,
        Long memberId,
        boolean isReply
) implements DomainEvent {
}
