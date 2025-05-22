package dooya.see.post.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder(toBuilder = true)
public record PostRequest(
        @NotBlank(message = "제목은 비어 있을 수 없습니다")
        String title,
        @NotBlank(message = "내용은 비어 있을 수 없습니다")
        String content
) {
}
