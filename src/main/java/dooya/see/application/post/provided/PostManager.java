package dooya.see.application.post.provided;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostUpdateRequest;

public interface PostManager {
    Post create(PostCreateRequest request, Long memberId);

    Post update(PostUpdateRequest request, Long postId, Long memberId);

    Post publish(Long postId, Long memberId);

    Post hide(Long postId, Long memberId);

    Post delete(Long postId, Long memberId);
}
