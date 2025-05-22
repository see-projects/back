package dooya.see.post.application.dto;

public record PostResult(
        Long id,
        String nickName,
        String title,
        String content
) {
}
