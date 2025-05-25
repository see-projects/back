package dooya.see.post.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    Page<Post> findAll(Pageable pageable);
    Optional<Post> findById(Long id);
}