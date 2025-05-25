package dooya.see.post.infrastructure;

import dooya.see.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
}
