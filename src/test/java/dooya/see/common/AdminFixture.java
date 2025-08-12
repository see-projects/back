package dooya.see.common;

import dooya.see.member.domain.Member;
import dooya.see.member.domain.Role;

public class AdminFixture {

    public static Member testAdmin() {
        return Member.builder()
                .email("test@see.com")
                .name("testName")
                .password("$2a$10$DOWSDdkg2YqXWB3S1.CzHeeI6qHtDc0lYBFfN4y3pFehUcbDoztYm")
                .nickName("testNickName")
                .role(Role.ADMIN)
                .build();
    }
}
