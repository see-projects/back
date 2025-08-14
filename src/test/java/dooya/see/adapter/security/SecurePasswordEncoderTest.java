package dooya.see.adapter.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurePasswordEncoderTest {
    @Test
    @DisplayName("비밀번호 암호화 후 올바른 비밀번호는 검증 성공하고 잘못된 비밀번호는 검증 실패한다")
    void securePasswordEncoder() {
        SecurePasswordEncoder securePasswordEncoder = new SecurePasswordEncoder();

        String passwordHash = securePasswordEncoder.encode("secret");

        assertThat(securePasswordEncoder.matches("secret", passwordHash)).isTrue();
        assertThat(securePasswordEncoder.matches("wrong", passwordHash)).isFalse();
    }
}
