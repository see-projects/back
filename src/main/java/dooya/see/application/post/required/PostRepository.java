package dooya.see.application.post.required;

import dooya.see.domain.post.Post;

public interface PostRepository {
    Post save(Post post);
}
