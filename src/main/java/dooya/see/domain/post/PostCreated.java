package dooya.see.domain.post;

import dooya.see.domain.shared.DomainEvent;

public record PostCreated(
        Long postId,
        Long memberId,
        PostCategory category,
        boolean publishImmediately
) implements DomainEvent {}
