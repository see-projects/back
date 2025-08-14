package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProfileTest {
    @Test
    @DisplayName("프로필 주소가 null이면 예외가 발생한다")
    void profileNotNull() {
        assertThatThrownBy(() -> new Profile(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("올바른 형식의 프로필 주소로 Profile을 생성할 수 있다")
    void validProfile() {
        Profile profile = new Profile("user123");
        
        assertThat(profile.address()).isEqualTo("user123");
    }

    @Test
    @DisplayName("빈 문자열로 Profile을 생성할 수 있다")
    void emptyProfile() {
        Profile profile = new Profile("");
        
        assertThat(profile.address()).isEqualTo("");
    }

    @Test
    @DisplayName("프로필 주소가 유효하지 않으면 예외가 발생한다")
    void profileInvalidFormat() {
        assertThatThrownBy(() -> new Profile("A"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile("한글 프로필"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 주소가 15자를 초과하면 예외가 발생한다")
    void profileTooLong() {
        assertThatThrownBy(() -> new Profile("longlonglonglonglonglongprofile"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로필 주소가 15자 이하일 때 생성할 수 있다")
    void profileWithin15Characters() {
        Profile profile = new Profile("123456789012345"); // 정확히 15자
        
        assertThat(profile.address()).isEqualTo("123456789012345");
    }
}
