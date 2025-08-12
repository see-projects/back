package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
}
