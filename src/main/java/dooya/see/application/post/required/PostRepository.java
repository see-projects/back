package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostStatus;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends Repository<Post, Long> {
    Post save(Post post);

    Optional<Post> findById(Long postId);

    List<Post> findByMemberId(Long memberId);

    List<Post> findByCategory(PostCategory category);

    List<Post> findByStatus(PostStatus status);

    List<Post> findByCategoryAndStatus(PostCategory category, PostStatus status);
}
