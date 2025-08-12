package dooya.see.application.post.provided;

import dooya.see.domain.post.PostResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostQueryService {
    Page<PostResult> getPosts(Pageable pageable);
    PostResult getPost(Long id);
}