package dooya.see.post.application.service;

import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;

public interface PostCreateService {
    PostResult createPost(String email, PostCommand command);
}
