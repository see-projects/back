package dooya.see.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberDetailTest {
    @Test
    @DisplayName("멤버 상세정보 클래스 존재여부")
    void memberDetailExist() {
        MemberDetail memberDetail = new MemberDetail();

        assertThat(memberDetail).isNotNull();
        assertThat(memberDetail.getRegisteredAt()).isNull();
    }
}
