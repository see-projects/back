package dooya.see.application.post.required;

import dooya.see.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchReader {
    Page<Post> searchByKeyword(String keyword, Pageable pageable);
}
