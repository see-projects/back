package dooya.see.post.application.service;

import dooya.see.post.application.dto.PostResult;

import java.util.List;

public interface PostQueryService {
    List<PostResult> getPosts();
    PostResult getPost(Long id);
}
