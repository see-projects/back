package dooya.see.domain.post;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagTest {
    private static final String TAG_NAME = "Java";
    private static final String TAG_TO_LOWERCASE = "java";
    private static final String TAG_DISPLAYNAME = "#java";

    @Nested
    class 태그_생성 {
        @Test
        void 태그를_생성할_수_있다() {
            Tag tag = new Tag(TAG_NAME);

            assertThat(tag.name()).isEqualTo(TAG_TO_LOWERCASE);
            assertThat(tag.displayName()).isEqualTo(TAG_DISPLAYNAME);
            assertThat(tag.urlSafe()).isEqualTo(TAG_TO_LOWERCASE);
        }
    }
}
