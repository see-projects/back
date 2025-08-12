package dooya.see.application.post.provided;

import dooya.see.domain.post.PostCommand;
import dooya.see.domain.post.PostResult;

public interface PostCreateService {
    PostResult createPost(String email, PostCommand command);
}
