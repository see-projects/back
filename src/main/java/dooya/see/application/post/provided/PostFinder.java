package dooya.see.application.post.provided;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.dto.PostSearchRequest;
import dooya.see.domain.post.PostStatus;

import java.util.List;

/**
 * 게시물 조회 Primary Port
 */
public interface PostFinder {
    Post find(Long postId);

    List<Post> findByMemberId(Long memberId);

    List<Post> findByCategory(PostCategory category);

    List<Post> findByStatus(PostStatus status);

    List<Post> findPublicPosts();

    List<Post> findPublicPostsByCategory(PostCategory category);

    List<Post> search(PostSearchRequest searchRequest);

    Post viewPost(Long postId, Long viewerId);
}