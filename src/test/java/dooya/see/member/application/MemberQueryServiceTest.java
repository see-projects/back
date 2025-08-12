package dooya.see.member.application;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.member.application.dto.MemberResult;
import dooya.see.member.application.service.impl.MemberQueryServiceImpl;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.Role;
import dooya.see.member.domain.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static dooya.see.common.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class MemberQueryServiceTest {

    @InjectMocks
    private MemberQueryServiceImpl userQueryService;

    @Mock
    private MemberRepository memberRepository;

    private final Member testMember = testUser();

    @DisplayName("이메일로 유저 조회 성공 단위테스트")
    @Test
    void user_GetUserByEmail_Success() {
        // Arrange
        given(memberRepository.findByEmail(testMember.getEmail())).willReturn(Optional.of(testMember));

        // Act
        MemberResult result = userQueryService.getUserByEmail(testMember.getEmail());

        // Assert
        assertAll(
                () -> assertThat(result.id()).isEqualTo(testMember.getId()),
                () -> assertThat(result.email()).isEqualTo(testMember.getEmail()),
                () -> assertThat(result.name()).isEqualTo(testMember.getName()),
                () -> assertThat(result.nickName()).isEqualTo(testMember.getNickName()),
                () -> assertThat(result.role()).isEqualTo(testMember.getRole())
        );
    }

    @DisplayName("이메일로 유저 조회 실패 단위테스트 - 존재하지 않는 이메일")
    @Test
    void user_GetUserByEmail_FailNotEmail() {
        // Arrange
        doThrow(new CustomException(ErrorCode.USER_NOT_FOUND)).when(memberRepository).findByEmail(testMember.getEmail());

        // Act && Assert
        assertThatCode(() -> userQueryService.getUserByEmail(testMember.getEmail()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");

        then(memberRepository).should(times(1)).findByEmail(testMember.getEmail());
    }

    @DisplayName("권한이 USER인 유저 모두 조회 성공 테스트")
    @Test
    void user_GetRoleUsers_Success() {
        // Arrange
        Member testMember1 = testUser();
        Member testMember2 = testUser();
        List<Member> members = List.of(testMember1, testMember2);

        // Act
        given(memberRepository.findByRole(Role.USER)).willReturn(members);

        // Assert
        List<MemberResult> results = userQueryService.getUsers();

        assertThat(results).hasSize(2);

        then(memberRepository).should(times(1)).findByRole(Role.USER);
    }
}
