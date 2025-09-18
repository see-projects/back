package dooya.see.domain.post;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostContentTest {
    @Test
    void 유효한_제목과_내용으로_PostContent_생성_시_정상적으로_생성된다() {
        String title = "테스트 제목";
        String body = "테스트 내용입니다.";
        
        PostContent postContent = new PostContent(title, body);
        
        assertThat(postContent.title()).isEqualTo(title);
        assertThat(postContent.body()).isEqualTo(body);
    }

    @Test
    void 제목이_null일_때_PostContent_생성_시_IllegalArgumentException이_발생한다() {
        String nullTitle = null;
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(nullTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 필수입니다");
    }

    @Test
    void 제목이_빈_문자열일_때_PostContent_생성_시_IllegalArgumentException이_발생한다() {
        String emptyTitle = "";
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(emptyTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 필수입니다");
    }

    @Test
    void 제목이_공백만_있을_때_PostContent_생성_시_IllegalArgumentException이_발생한다() {
        String blankTitle = "   ";
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(blankTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 필수입니다");
    }

    @Test
    void 내용이_null일_때_PostContent_생성_시_IllegalArgumentException이_발생한다() {
        String validTitle = "유효한 제목";
        String nullBody = null;
        
        assertThatThrownBy(() -> new PostContent(validTitle, nullBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 내용은 필수입니다");
    }

    @Test
    void 내용이_빈_문자열일_때_PostContent_생성_시_IllegalArgumentException이_발생한다() {
        String validTitle = "유효한 제목";
        String emptyBody = "";
        
        assertThatThrownBy(() -> new PostContent(validTitle, emptyBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 내용은 필수입니다");
    }

    @Test
    void 제목이_100자를_초과할_때_PostContent_생성_시_IllegalArgumentException이_발생한다() {
        String tooLongTitle = "a".repeat(101);
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(tooLongTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 100자를 초과할 수 없습니다");
    }

    @Test
    void 제목이_정확히_100자일_때_PostContent_생성이_성공한다() {
        String maxLengthTitle = "a".repeat(100);
        String validBody = "유효한 내용";
        
        PostContent postContent = new PostContent(maxLengthTitle, validBody);
        
        assertThat(postContent.title()).isEqualTo(maxLengthTitle);
        assertThat(postContent.body()).isEqualTo(validBody);
    }

    @Test
    void 내용이_50000자를_초과할_때_PostContent_생성_시_IllegalArgumentException이_발생한다() {
        String validTitle = "유효한 제목";
        String tooLongBody = "a".repeat(50001);
        
        assertThatThrownBy(() -> new PostContent(validTitle, tooLongBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 내용은 50,000자를 초과할 수 없습니다");
    }

    @Test
    void 내용이_정확히_50000자일_때_PostContent_생성이_성공한다() {
        String validTitle = "유효한 제목";
        String maxLengthBody = "a".repeat(50000);
        
        PostContent postContent = new PostContent(validTitle, maxLengthBody);
        
        assertThat(postContent.title()).isEqualTo(validTitle);
        assertThat(postContent.body()).isEqualTo(maxLengthBody);
    }
}
