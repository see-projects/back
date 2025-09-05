package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class CommentContentTest {

    @DisplayName("유효한 댓글 내용으로 CommentContent 객체가 정상 생성된다")
    @Test
    void createValidCommentContent() {
        String validText = "좋은 글이네요!";

        CommentContent content = new CommentContent(validText);

        assertThat(content.text()).isEqualTo(validText);
    }

    @DisplayName("댓글 내용의 앞뒤 공백이 자동으로 제거된다")
    @Test
    void trimWhitespaceFromContent() {
        String textWithWhitespace = " 좋은 글이네요! ";

        CommentContent content = new CommentContent(textWithWhitespace);

        assertThat(content.text()).isEqualTo("좋은 글이네요!");
    }

    @DisplayName("댓글 내용이 null이면 IllegalArgumentException이 발생한다")
    @Test
    void rejectNullContent() {
        assertThatThrownBy(() -> new CommentContent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 필수 입니다");
    }

    @DisplayName("댓글 내용이 공백 문자만 있으면 IllegalArgumentException이 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n", " \n\t "})
    void rejectWhitespaceOnlyContent(String emptyText) {
        assertThatThrownBy(() -> new CommentContent(emptyText))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 1자 이상이어야 합니다");
    }

    @DisplayName("댓글 내용이 1000자를 초과하면 IllegalArgumentException이 발생한다")
    @Test
    void rejectTooLongContent() {
        String longText = "a".repeat(1001);

        assertThatThrownBy(() -> new CommentContent(longText))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 1000자를 초과할 수 없습니다");
    }

    @DisplayName("정확히 1000자의 댓글 내용은 정상적으로 허용된다")
    @Test
    void acceptExactly1000CharactersContent() {
        String exactLengthText = "a".repeat(1000);

        assertThatCode(() -> new CommentContent(exactLengthText))
                .doesNotThrowAnyException();
    }

    @DisplayName("1자의 댓글 내용도 정상적으로 허용된다")
    @Test
    void acceptSingleCharacterContent() {
        String singleChar = "a";

        CommentContent content = new CommentContent(singleChar);

        assertThat(content.text()).isEqualTo("a");
    }
}
