package dooya.see.post.domain;

import java.util.List;

public interface PostRepository {
    Post save(Post post);
    List<Post> findAll();
}