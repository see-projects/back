package dooya.see.post.presentation.dto;

import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import org.springframework.data.domain.Page;

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
                result.content(),
                result.createdDate(),
                result.updatedDate()
        );
    }

    public static Page<PostResponse> toResponses(Page<PostResult> results) {
        return results.map(PostPresentationMapper::toResponse);
    }
}
