package dooya.see.domain.post.event;

import dooya.see.domain.shared.DomainEvent;

public record PostViewed(
        Long postId,
        Long memberId  // 조회한 사용자 ID (null이면 익명 조회)
) implements DomainEvent {}
