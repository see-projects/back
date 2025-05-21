package dooya.see.common;

import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;

public class AdminFixture {

    public static User testAdmin() {
        return User.builder()
                .email("test@see.com")
                .name("testName")
                .password("$2a$10$DOWSDdkg2YqXWB3S1.CzHeeI6qHtDc0lYBFfN4y3pFehUcbDoztYm")
                .nickName("testNickName")
                .role(Role.ADMIN)
                .build();
    }
}
