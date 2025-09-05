package dooya.see.domain.post.event;

import dooya.see.domain.post.CommentStatus;
import dooya.see.domain.shared.DomainEvent;

public record CommentHidden(
        Long commentId,
        Long postId,
        Long memberId,
        CommentStatus previousStatus
) implements DomainEvent {
}
