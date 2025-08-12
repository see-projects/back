package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    Page<Post> findAll(Pageable pageable);
    Optional<Post> findById(Long id);
}