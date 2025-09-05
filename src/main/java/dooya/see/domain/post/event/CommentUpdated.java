package dooya.see.domain.post.event;

import dooya.see.domain.shared.DomainEvent;

public record CommentUpdated(
        Long commentId,
        Long postId,
        Long memberId,
        String previousContent,
        String newContent
) implements DomainEvent {
}
