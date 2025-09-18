package dooya.see.domain.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class CommentContentTest {
    @Test
    void 유효한_댓글_내용으로_CommentContent_객체가_정상_생성된다() {
        String validText = "좋은 글이네요!";

        CommentContent content = new CommentContent(validText);

        assertThat(content.text()).isEqualTo(validText);
    }

    @Test
    void 댓글_내용의_앞뒤_공백이_자동으로_제거된다() {
        String textWithWhitespace = " 좋은 글이네요! ";

        CommentContent content = new CommentContent(textWithWhitespace);

        assertThat(content.text()).isEqualTo("좋은 글이네요!");
    }

    @Test
    void 댓글_내용이_null이면_IllegalArgumentException이_발생한다() {
        assertThatThrownBy(() -> new CommentContent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 필수 입니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n", " \n\t "})
    void 댓글_내용이_공백_문자만_있으면_IllegalArgumentException이_발생한다(String emptyText) {
        assertThatThrownBy(() -> new CommentContent(emptyText))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 1자 이상이어야 합니다");
    }

    @Test
    void 댓글_내용이_1000자를_초과하면_IllegalArgumentException이_발생한다() {
        String longText = "a".repeat(1001);

        assertThatThrownBy(() -> new CommentContent(longText))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 1000자를 초과할 수 없습니다");
    }

    @Test
    void 정확히_1000자의_댓글_내용은_정상적으로_허용된다() {
        String exactLengthText = "a".repeat(1000);

        assertThatCode(() -> new CommentContent(exactLengthText))
                .doesNotThrowAnyException();
    }

    @Test
    void 일의자리의_댓글_내용도_정상적으로_허용된다() {
        String singleChar = "a";

        CommentContent content = new CommentContent(singleChar);

        assertThat(content.text()).isEqualTo("a");
    }
}
