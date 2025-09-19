package dooya.see.domain.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class CommentContentTest {
    private static final String VALID_TEXT = "좋은 글이네요!";
    private static final String TEXT_WITH_WHITESPACE = " 좋은 글이네요! ";
    private static final String SINGLE_CHAR = "a";
    private static final String MAX_LENGTH_TEXT = "a".repeat(1000);
    private static final String TOO_LONG_TEXT = "a".repeat(1001);

    @Nested
    class 댓글_내용_생성 {
        @Test
        void 유효한_내용으로_생성할_수_있다() {
            CommentContent content = new CommentContent(VALID_TEXT);

            assertThat(content.text()).isEqualTo(VALID_TEXT);
        }

        @Test
        void 한_글자_내용으로_생성할_수_있다() {
            CommentContent content = new CommentContent(SINGLE_CHAR);

            assertThat(content.text()).isEqualTo(SINGLE_CHAR);
        }

        @Test
        void 최대_길이_내용으로_생성할_수_있다() {
            assertThatCode(() -> new CommentContent(MAX_LENGTH_TEXT))
                    .doesNotThrowAnyException();
        }

        @Test
        void 앞뒤_공백이_자동으로_제거된다() {
            CommentContent content = new CommentContent(TEXT_WITH_WHITESPACE);

            assertThat(content.text()).isEqualTo(VALID_TEXT);
        }
    }

    @Nested
    class 댓글_내용_생성_실패 {
        @Test
        void null_내용으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new CommentContent(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글 내용은 필수 입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", " \n\t "})
        void 공백만_있는_내용으로_생성_시_예외가_발생한다(String emptyText) {
            assertThatThrownBy(() -> new CommentContent(emptyText))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글 내용은 1자 이상이어야 합니다");
        }

        @Test
        void 길이_초과_내용으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new CommentContent(TOO_LONG_TEXT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글 내용은 1000자를 초과할 수 없습니다");
        }
    }

    @Nested
    class 값_객체_특성 {
        @Test
        void 동일한_내용을_가진_댓글_내용은_같다() {
            CommentContent content1 = new CommentContent(VALID_TEXT);
            CommentContent content2 = new CommentContent(VALID_TEXT);

            assertThat(content1).isEqualTo(content2);
            assertThat(content1.hashCode()).isEqualTo(content2.hashCode());
        }

        @Test
        void 다른_내용을_가진_댓글_내용은_다르다() {
            CommentContent content1 = new CommentContent(VALID_TEXT);
            CommentContent content2 = new CommentContent("다른 내용");

            assertThat(content1).isNotEqualTo(content2);
        }

        @Test
        void 공백_처리된_내용과_원본이_같다() {
            CommentContent trimmedContent = new CommentContent(TEXT_WITH_WHITESPACE);
            CommentContent originalContent = new CommentContent(VALID_TEXT);

            assertThat(trimmedContent).isEqualTo(originalContent);
        }
    }
}