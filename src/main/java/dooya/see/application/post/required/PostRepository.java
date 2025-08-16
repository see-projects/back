package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import org.springframework.data.repository.Repository;

public interface PostRepository extends Repository<Post, Long> {
    Post save(Post post);
}
