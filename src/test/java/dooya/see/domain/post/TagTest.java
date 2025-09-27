package dooya.see.domain.post;

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

            assertThat(tag.name()).isEqualTo(TAG_TO_LOWERCASE);
            assertThat(tag.displayName()).isEqualTo(TAG_DISPLAYNAME);
            assertThat(tag.urlSafe()).isEqualTo(TAG_TO_LOWERCASE);
        }

        @Test
        void 한글_태그명으로도_생성할_수_있다() {
            Tag tag = new Tag(TAG_KOREANNAME);

            assertThat(tag.name()).isEqualTo(TAG_KOREANNAME);
            assertThat(tag.displayName()).isEqualTo(TAG_DISPLAYKOREANNAME);
        }

        @Test
        void null_태그명으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new Tag(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 빈_문자열_태그명으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new Tag(""))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 초과_태그명으로_생성_시_예외가_발생한다() {
            String longTagName = "a".repeat(21);
            
            assertThatThrownBy(() -> new Tag(longTagName))
                .isInstanceOf(IllegalArgumentException.class);
        }
        
        @Test
        void 특수문자_포함_태그명으로_생성_시_예외가_발생한다() {
            assertThatThrownBy(() -> new Tag(TAG_SpecialCharacters))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
