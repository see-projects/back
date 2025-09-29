package dooya.see.application.post.provided;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.dto.PostCreateRequest;
import dooya.see.domain.post.dto.PostUpdateRequest;

/**
 * 게시물 관리 Primary Port
 */
public interface PostManager {
    Post create(PostCreateRequest request, Long memberId);

    Post update(PostUpdateRequest request, Long postId, Long memberId);

    Post publish(Long postId, Long memberId);

    Post hide(Long postId, Long memberId);

    Post delete(Long postId, Long memberId);

    Post likePost(Long postId, Long memberId);

    Post unlikePost(Long postId, Long memberId);
}