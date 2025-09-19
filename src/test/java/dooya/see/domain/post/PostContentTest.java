package dooya.see.domain.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostContentTest {
    private static final String VALID_TITLE = "테스트 제목";
    private static final String VALID_BODY = "테스트 내용입니다.";
    private static final String MAX_LENGTH_TITLE = "a".repeat(100);
    private static final String TOO_LONG_TITLE = "a".repeat(101);
    private static final String MAX_LENGTH_BODY = "a".repeat(50000);
    private static final String TOO_LONG_BODY = "a".repeat(50001);

    @Nested
    class 게시글_콘텐츠_생성 {
        @Test
        void 유효한_제목과_내용으로_생성할_수_있다() {
            PostContent postContent = new PostContent(VALID_TITLE, VALID_BODY);

            assertThatPostContentCreated(postContent, VALID_TITLE, VALID_BODY);
        }

        @Test
        void 최대_길이_제목과_내용으로_생성할_수_있다() {
            PostContent postContent = new PostContent(MAX_LENGTH_TITLE, MAX_LENGTH_BODY);

            assertThatPostContentCreated(postContent, MAX_LENGTH_TITLE, MAX_LENGTH_BODY);
        }

        private void assertThatPostContentCreated(PostContent postContent, String expectedTitle, String expectedBody) {
            assertThat(postContent.title()).isEqualTo(expectedTitle);
            assertThat(postContent.body()).isEqualTo(expectedBody);
        }
    }

    @Nested
    class 제목_검증_실패 {
        @Test
        void null_제목으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new PostContent(null, VALID_BODY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 제목은 필수입니다");
        }

        @Test
        void 빈_제목으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new PostContent("", VALID_BODY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 제목은 필수입니다");
        }

        @Test
        void 공백만_있는_제목으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new PostContent("   ", VALID_BODY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 제목은 필수입니다");
        }

        @Test
        void 길이_초과_제목으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new PostContent(TOO_LONG_TITLE, VALID_BODY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 제목은 100자를 초과할 수 없습니다");
        }
    }

    @Nested
    class 내용_검증_실패 {
        @Test
        void null_내용으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new PostContent(VALID_TITLE, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 내용은 필수입니다");
        }

        @Test
        void 빈_내용으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new PostContent(VALID_TITLE, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 내용은 필수입니다");
        }

        @Test
        void 길이_초과_내용으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new PostContent(VALID_TITLE, TOO_LONG_BODY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 내용은 50,000자를 초과할 수 없습니다");
        }
    }

    @Nested
    class 값_객체_특성 {
        @Test
        void 동일한_제목과_내용을_가진_콘텐츠는_같다() {
            PostContent content1 = new PostContent(VALID_TITLE, VALID_BODY);
            PostContent content2 = new PostContent(VALID_TITLE, VALID_BODY);

            assertThat(content1).isEqualTo(content2);
            assertThat(content1.hashCode()).isEqualTo(content2.hashCode());
        }

        @Test
        void 다른_제목이나_내용을_가진_콘텐츠는_다르다() {
            PostContent original = new PostContent(VALID_TITLE, VALID_BODY);
            PostContent differentTitle = new PostContent("다른 제목", VALID_BODY);
            PostContent differentBody = new PostContent(VALID_TITLE, "다른 내용");

            assertThat(original).isNotEqualTo(differentTitle);
            assertThat(original).isNotEqualTo(differentBody);
        }

        @Test
        void 불변_객체로_동작한다() {
            PostContent content = new PostContent(VALID_TITLE, VALID_BODY);

            // Record이므로 자동으로 불변성이 보장됨
            assertThat(content.title()).isEqualTo(VALID_TITLE);
            assertThat(content.body()).isEqualTo(VALID_BODY);
        }
    }
}