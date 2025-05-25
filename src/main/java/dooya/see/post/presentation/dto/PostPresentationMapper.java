package dooya.see.post.presentation.dto;

import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.dto.PostResult;
import dooya.see.user.application.dto.UserResult;
import dooya.see.user.presentation.dto.UserPresentationMapper;
import dooya.see.user.presentation.dto.UserResponse;

import java.util.List;

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

    public static List<PostResponse> toResponses(List<PostResult> results) {
        return results.stream()
                .map(PostPresentationMapper::toResponse)
                .toList();
    }
}
