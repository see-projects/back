package dooya.see.application.post.provided;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostUpdateRequest;

public interface PostManager {
    Post create(PostCreateRequest request, Long memberId);

    Post update(Long postId, PostUpdateRequest request);

    Post publish(Long postId);

    Post hide(Long postId);

    Post delete(Long postId);

    Post incrementViewCount(Long postId);

    Post incrementLikeCount(Long postId);

    Post incrementCommentCount(Long postId);
}
