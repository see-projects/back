package dooya.see.post.application.dto;

import dooya.see.post.domain.Post;
import dooya.see.user.domain.User;

public class PostApplicationMapper {

    public static Post toEntity(PostCommand command, User user) {
        return Post.createPost(
                command.title(),
                command.content(),
                user
        );
    }

    public static PostResult toResult(Post post) {
        return new PostResult(
                post.getId(),
                post.getUser().getNickName(),
                post.getTitle(),
                post.getContent()
        );
    }
}
