package dooya.see.domain.post.event;

import dooya.see.domain.shared.DomainEvent;

public record PostLiked(
        Long postId,
        Long memberId
) implements DomainEvent {}
