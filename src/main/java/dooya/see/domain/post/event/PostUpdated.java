package dooya.see.domain.post.event;

import dooya.see.domain.shared.DomainEvent;

/**
 * 게시글이 수정되었을 때 발생하는 도메인 이벤트
 */
public record PostUpdated(
    Long postId,
    Long memberId,
    boolean titleChanged,
    boolean bodyChanged,
    boolean categoryChanged,
    boolean tagsChanged
) implements DomainEvent {}
