package dooya.see.domain.post;

import jakarta.persistence.Embeddable;

@Embeddable
public record PostContent(
        String title,
        String body
) {
    public PostContent {
        validateTitle(title);
        validateBody(body);
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 제목은 필수입니다");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("게시글 제목은 100자를 초과할 수 없습니다");
        }
    }

    private void validateBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("게시글 내용은 필수입니다");
        }
        if (body.length() > 50000) {
            throw new IllegalArgumentException("게시글 내용은 50,000자를 초과할 수 없습니다");
        }
    }
}
