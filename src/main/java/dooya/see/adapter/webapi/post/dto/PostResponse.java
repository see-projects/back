package dooya.see.adapter.webapi.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PostResponse(

        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "사용자 닉네임", example = "See")
        String nickName,

        @Schema(description = "게시글 제목", example = "이 커뮤니티 진짜 좋네!!")
        String title,

        @Schema(description = "게시글 내용", example = "이 게시글은 ~~~~")
        String content,

        @Schema(description = "게시글 작성 날짜", example = "~~~~~")
        LocalDateTime createdDate,

        @Schema(description = "게시글 업데이트 날짜", example = "~~~~~")
        LocalDateTime updatedDate
) {
}
