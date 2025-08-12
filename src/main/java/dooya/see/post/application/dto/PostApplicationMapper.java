package dooya.see.post.application.dto;

import dooya.see.post.domain.Post;
import dooya.see.member.domain.Member;
import org.springframework.data.domain.Page;

public class PostApplicationMapper {

    public static Post toEntity(PostCommand command, Member member) {
        return Post.createPost(
                command.title(),
                command.content(),
                member
        );
    }

    public static PostResult toResult(Post post) {
        return new PostResult(
                post.getId(),
                post.getMember().getNickName(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static Page<PostResult> toResults(Page<Post> posts) {
        return posts.map(PostApplicationMapper::toResult);
    }
}
