package dooya.see.post.presentation.dto;

public record PostResponse(
        Long id,
        String nickName,
        String title,
        String content
) {
}
