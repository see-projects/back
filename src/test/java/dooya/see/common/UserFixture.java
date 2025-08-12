package dooya.see.common;

import dooya.see.member.application.dto.*;
import dooya.see.member.domain.Member;
import dooya.see.member.presentation.dto.PasswordUpdateRequest;
import dooya.see.member.presentation.dto.MemberRegisterRequest;
import dooya.see.member.domain.Role;
import dooya.see.member.presentation.dto.NickNameUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static MemberRegisterRequest signUpRequest() {
        return new MemberRegisterRequest("dooya@see.com", "testName", "testPassword", "testNickName");
    }

    public static NickNameUpdateRequest nickNameUpdateRequest() {
        return new NickNameUpdateRequest("updateNickName");
    }

    public static PasswordUpdateRequest passwordUpdateRequest() {
        return new PasswordUpdateRequest("testPassword", "updatePassword");
    }

    public static PasswordUpdateRequest passwordUpdateRequestFail() {
        return new PasswordUpdateRequest("failPassword", "updatePassword");
    }

    public static MemberRegisterCommand signUpCommand() {
        return new MemberRegisterCommand("dooya@see.com", "testName", "testPassword", "testNickName");
    }

    public static NickNameUpdateCommand updateCommand() {
        return new NickNameUpdateCommand("updateNickName");
    }

    public static PasswordUpdateCommand passwordUpdateCommand() {
        return new PasswordUpdateCommand("testPassword", "updatePassword");
    }

    public static Member createTestUser(MemberRegisterCommand command) {
        Member testMember = Member.signUpUser(
                command.email(),
                command.name(),
                command.password(),
                command.nickName(),
                "https://fake-s3/profile.jpeg",
                Role.of("USER")
        );
        ReflectionTestUtils.setField(testMember, "id", 1L);
        return testMember;
    }

    public static Member testUser() {
        return Member.builder()
                .email("test@see.com")
                .name("testName")
                .password("$2a$10$DOWSDdkg2YqXWB3S1.CzHeeI6qHtDc0lYBFfN4y3pFehUcbDoztYm")
                .nickName("testNickName")
                .role(Role.of("USER"))
                .build();
    }

    public static MemberResult testUserResult() {
        return new MemberResult(1L, "dooya@see.com", "testName", "testNickName", "https://fake-s3/profile.jpeg", Role.of("USER"));
    }

    public static PasswordUpdateResult passwordUpdateResult() {
        return new PasswordUpdateResult("비밀번호가 성공적으로 변경되었습니다.");
    }

    public static Member mockUser(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email("test@see.com")
                .name("testName")
                .password(passwordEncoder.encode("testPassword"))
                .nickName("testNickName")
                .role(Role.of("USER"))
                .build();
    }
}
