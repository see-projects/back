package dooya.see.post.presentation.dto;

import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;

public class PostPresentationMapper {

    public static PostCommand toCommand(PostRequest request) {
        return new PostCommand(
                request.title(),
                request.content()
        );
    }

    public static PostResponse toResponse(PostResult result) {
        return new PostResponse(
                result.id(),
                result.nickName(),
                result.title(),
                result.content()
        );
    }
}
