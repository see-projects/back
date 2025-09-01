package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileTest {
    @DisplayName("프로필 주소가 null이면 예외가 발생한다")
    @Test
    void createProfile_withNullAddress_throwsException() {
        assertThatThrownBy(() -> new Profile(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("올바른 형식의 프로필 주소로 Profile을 생성할 수 있다")
    @Test
    void createProfile_withValidAddress_success() {
        Profile profile = new Profile("user123");
        
        assertThat(profile.address()).isEqualTo("user123");
    }

    @DisplayName("빈 문자열로 Profile을 생성할 수 있다")
    @Test
    void createProfile_withEmptyString_success() {
        Profile profile = new Profile("");
        
        assertThat(profile.address()).isEqualTo("");
    }

    @DisplayName("프로필 주소가 유효하지 않은 형식이면 예외가 발생한다")
    @Test
    void createProfile_withInvalidFormat_throwsException() {
        assertThatThrownBy(() -> new Profile("A"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile("한글 프로필"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("프로필 주소가 15자를 초과하면 예외가 발생한다")
    @Test
    void createProfile_withTooLongAddress_throwsException() {
        assertThatThrownBy(() -> new Profile("longlonglonglonglonglongprofile"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("프로필 주소가 정확히 15자일 때 생성할 수 있다")
    @Test
    void createProfile_withExactly15Characters_success() {
        Profile profile = new Profile("123456789012345"); // 정확히 15자
        
        assertThat(profile.address()).isEqualTo("123456789012345");
    }
}
