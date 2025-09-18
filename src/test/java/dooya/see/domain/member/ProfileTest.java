package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileTest {
    @Test
    void 프로필_주소가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Profile(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 올바른_형식의_프로필_주소로_Profile을_생성할_수_있다() {
        Profile profile = new Profile("user123");
        
        assertThat(profile.address()).isEqualTo("user123");
    }

    @Test
    void 빈_문자열로_Profile을_생성할_수_있다() {
        Profile profile = new Profile("");
        
        assertThat(profile.address()).isEqualTo("");
    }

    @Test
    void 프로필_주소가_유효하지_않은_형식이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Profile("A"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile("한글 프로필"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 프로필_주소가_15자를_초과하면_예외가_발생한다() {
        assertThatThrownBy(() -> new Profile("longlonglonglonglonglongprofile"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 프로필_주소가_정확히_15자일_때_생성할_수_있다() {
        Profile profile = new Profile("123456789012345"); // 정확히 15자
        
        assertThat(profile.address()).isEqualTo("123456789012345");
    }
}
