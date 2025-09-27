package dooya.see.domain.post;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagTest {
    private static final String TAG_NAME = "Java";
    private static final String TAG_TO_LOWERCASE = "java";
    private static final String TAG_DISPLAYNAME = "#java";
    private static final String TAG_KOREANNAME = "스프링부트";
    private static final String TAG_DISPLAYKOREANNAME = "#스프링부트";
    private static final String TAG_SpecialCharacters = "java@spring";

    @Nested
    class 태그_생성 {
        @Test
        void 태그를_생성할_수_있다() {
            Tag tag = new Tag(TAG_NAME);

            assertTagNameEqualsExpected(tag.name(), TAG_TO_LOWERCASE);
            assertTagNameEqualsExpected(tag.displayName(), TAG_DISPLAYNAME);
            assertTagNameEqualsExpected(tag.urlSafe(), TAG_TO_LOWERCASE);
        }

        @Test
        void 한글_태그명으로도_생성할_수_있다() {
            Tag tag = new Tag(TAG_KOREANNAME);

            assertTagNameEqualsExpected(tag.name(), TAG_KOREANNAME);
            assertTagNameEqualsExpected(tag.displayName(), TAG_DISPLAYKOREANNAME);
        }

        @Test
        void null_태그명으로_생성_시_예외가_발생한다() {
            assertTagCreationFails(null);
        }

        @Test
        void 빈_문자열_태그명으로_생성_시_예외가_발생한다() {
            assertTagCreationFails("");
        }

        @Test
        void 초과_태그명으로_생성_시_예외가_발생한다() {
            String longTagName = "a".repeat(21);

            assertTagCreationFails(longTagName);
        }

        @Test
        void 특수문자_포함_태그명으로_생성_시_예외가_발생한다() {
            assertTagCreationFails(TAG_SpecialCharacters);
        }

        private static void assertTagNameEqualsExpected(String tag, String tagToLowercase) {
            Assertions.assertThat(tag).isEqualTo(tagToLowercase);
        }

        private static void assertTagCreationFails(String name) {
            assertThatThrownBy(() -> new Tag(name))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class 태그_동등성 {
        @Test
        void 같은_이름의_태그는_동등하다() {
            Tag tag1 = new Tag(TAG_NAME);
            Tag tag2 = new Tag(TAG_TO_LOWERCASE);

            assertThat(tag1).isEqualTo(tag2);
            assertThat(tag1.hashCode()).isEqualTo(tag2.hashCode());
        }
    }
}
