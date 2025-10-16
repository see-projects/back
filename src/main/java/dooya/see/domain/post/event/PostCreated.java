package dooya.see.domain.post.event;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.shared.DomainEvent;

public record PostCreated(
        Long postId,
        Long memberId,
        PostCategory category,
        boolean publishImmediately,
        Post post
) implements DomainEvent {
}
