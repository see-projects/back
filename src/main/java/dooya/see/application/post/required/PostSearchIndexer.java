package dooya.see.application.post.required;

import dooya.see.domain.post.Post;

public interface PostSearchIndexer {
    void index(Post post);
    void delete(Long postId);
}
