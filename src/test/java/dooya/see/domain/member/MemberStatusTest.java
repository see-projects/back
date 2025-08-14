package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberStatusTest {
    @Test
    @DisplayName("MemberStatus enum의 ACTIVE와 DEACTIVATED가 정의되어 있다")
    void memberStatusDefault() {
        assertThat(MemberStatus.ACTIVE).isNotNull();
        assertThat(MemberStatus.DEACTIVATED).isNotNull();
    }
}
