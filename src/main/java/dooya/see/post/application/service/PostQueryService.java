package dooya.see.post.application.service;

import dooya.see.post.application.dto.PostResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostQueryService {
    Page<PostResult> getPosts(Pageable pageable);
    PostResult getPost(Long id);
}