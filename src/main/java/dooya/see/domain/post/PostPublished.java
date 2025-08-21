package dooya.see.domain.post;

import dooya.see.domain.shared.DomainEvent;

public record PostPublished(
        Long postId,
        Long memberId
) implements DomainEvent {
}
