package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CommentContentTest {
    @DisplayName("")
    @Test
    void createValidCommentContent() {
        String validText = "좋은 글이네요!";

        CommentContent content = new CommentContent(validText);

        assertThat(content.text()).isEqualTo(validText);
    }

    @DisplayName("")
    @Test
    void rejectNullContent() {
        assertThatThrownBy(() -> new CommentContent(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("")
    @Test
    void rejectEmptyContent() {
        assertThatThrownBy(() -> new CommentContent(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("")
    @Test
    void rejectWhitespaceOnlyContent() {
        assertThatThrownBy(() -> new CommentContent(" \n\t "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("")
    @Test
    void rejectTooLongContent() {
        String longText = "a".repeat(1001);

        assertThatThrownBy(() -> new CommentContent(longText))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("")
    @Test
    void acceptExactly1000CharactersContent() {
        String exactLengthText = "a".repeat(1000);

        assertThatCode(() -> new CommentContent(exactLengthText))
                .doesNotThrowAnyException();
    }

    @DisplayName("")
    @Test
    void trimWhitespaceFromContent() {
        String textWithWhitespace = " 좋은 글이네요! ";

        CommentContent content = new CommentContent(textWithWhitespace);

        assertThat(content.text()).isEqualTo("좋은 글이네요!");
    }
}
