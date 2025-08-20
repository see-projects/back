package dooya.see.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostContentTest {
    @Test
    @DisplayName("유효한 제목과 내용으로 PostContent 생성 시 정상적으로 생성된다")
    void createValidPostContent() {
        String title = "테스트 제목";
        String body = "테스트 내용입니다.";
        
        PostContent postContent = new PostContent(title, body);
        
        assertThat(postContent.title()).isEqualTo(title);
        assertThat(postContent.body()).isEqualTo(body);
    }

    @Test
    @DisplayName("제목이 null일 때 PostContent 생성 시 IllegalArgumentException이 발생한다")
    void createPostContentWithNullTitle() {
        String nullTitle = null;
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(nullTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 필수입니다");
    }

    @Test
    @DisplayName("제목이 빈 문자열일 때 PostContent 생성 시 IllegalArgumentException이 발생한다")
    void createPostContentWithEmptyTitle() {
        String emptyTitle = "";
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(emptyTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 필수입니다");
    }

    @Test
    @DisplayName("제목이 공백만 있을 때 PostContent 생성 시 IllegalArgumentException이 발생한다")
    void createPostContentWithBlankTitle() {
        String blankTitle = "   ";
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(blankTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 필수입니다");
    }

    @Test
    @DisplayName("내용이 null일 때 PostContent 생성 시 IllegalArgumentException이 발생한다")
    void createPostContentWithNullBody() {
        String validTitle = "유효한 제목";
        String nullBody = null;
        
        assertThatThrownBy(() -> new PostContent(validTitle, nullBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 내용은 필수입니다");
    }

    @Test
    @DisplayName("내용이 빈 문자열일 때 PostContent 생성 시 IllegalArgumentException이 발생한다")
    void createPostContentWithEmptyBody() {
        String validTitle = "유효한 제목";
        String emptyBody = "";
        
        assertThatThrownBy(() -> new PostContent(validTitle, emptyBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 내용은 필수입니다");
    }

    @Test
    @DisplayName("제목이 100자를 초과할 때 PostContent 생성 시 IllegalArgumentException이 발생한다")
    void createPostContentWithTooLongTitle() {
        String tooLongTitle = "a".repeat(101);
        String validBody = "유효한 내용";
        
        assertThatThrownBy(() -> new PostContent(tooLongTitle, validBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 제목은 100자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("제목이 정확히 100자일 때 PostContent 생성이 성공한다")
    void createPostContentWithMaxLengthTitle() {
        String maxLengthTitle = "a".repeat(100);
        String validBody = "유효한 내용";
        
        PostContent postContent = new PostContent(maxLengthTitle, validBody);
        
        assertThat(postContent.title()).isEqualTo(maxLengthTitle);
        assertThat(postContent.body()).isEqualTo(validBody);
    }

    @Test
    @DisplayName("내용이 50,000자를 초과할 때 PostContent 생성 시 IllegalArgumentException이 발생한다")
    void createPostContentWithTooLongBody() {
        String validTitle = "유효한 제목";
        String tooLongBody = "a".repeat(50001);
        
        assertThatThrownBy(() -> new PostContent(validTitle, tooLongBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("게시글 내용은 50,000자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("내용이 정확히 50,000자일 때 PostContent 생성이 성공한다")
    void createPostContentWithMaxLengthBody() {
        String validTitle = "유효한 제목";
        String maxLengthBody = "a".repeat(50000);
        
        PostContent postContent = new PostContent(validTitle, maxLengthBody);
        
        assertThat(postContent.title()).isEqualTo(validTitle);
        assertThat(postContent.body()).isEqualTo(maxLengthBody);
    }
}
