package dooya.see.post.application.dto;

import dooya.see.post.domain.Post;
import dooya.see.user.domain.User;
import org.springframework.data.domain.Page;

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

    public static Page<PostResult> toResults(Page<Post> posts) {
        return posts.map(PostApplicationMapper::toResult);
    }
}
