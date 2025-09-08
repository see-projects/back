package dooya.see.domain.post;

import jakarta.persistence.Embeddable;

@Embeddable
public record CommentContent(String text) {
    public CommentContent {
        if (text == null) {
            throw new IllegalArgumentException("댓글 내용은 필수 입니다");
        }

        text = text.trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 1자 이상이어야 합니다");
        }

        if (text.length() > 1000) {
            throw new IllegalArgumentException("댓글 내용은 1000자를 초과할 수 없습니다");
        }
    }
}
