package dooya.see.domain.post.event;

import dooya.see.domain.post.PostStatus;
import dooya.see.domain.shared.DomainEvent;

/**
 * 게시글이 삭제되었을 때 발생하는 도메인 이벤트
 */
public record PostDeleted(
    Long postId,
    Long memberId,
    PostStatus previousStatus
) implements DomainEvent {}
