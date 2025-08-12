package dooya.see.member.application;

import dooya.see.common.config.UserProfileProperties;
import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.member.application.dto.MemberResult;
import dooya.see.member.application.dto.MemberRegisterCommand;
import dooya.see.member.application.service.impl.MemberRegisterServiceImpl;
import dooya.see.member.application.service.MemberValidator;
import dooya.see.member.domain.Member;
import dooya.see.member.domain.Role;
import dooya.see.member.domain.MemberRepository;
import dooya.see.common.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberRegisterServiceTest {

    @InjectMocks
    private MemberRegisterServiceImpl userSignUpService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberValidator memberValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserProfileProperties userProfileProperties;

    @DisplayName("유저 회원가입 성공 단위테스트")
    @Test
    void user_SignUp_Success() {
        // Arrange
        MemberRegisterCommand command = UserFixture.signUpCommand();
        Member testMember = UserFixture.createTestUser(command);

        doNothing().when(memberValidator).validateDuplicateEmail(command.email());
        given(passwordEncoder.encode(command.password())).willReturn(testMember.getPassword());
        given(userProfileProperties.getDefaultImageUrl()).willReturn("default-image-url");
        given(memberRepository.save(any(Member.class))).willReturn(testMember);

        // Act
        MemberResult result = userSignUpService.userSignUp(command);

        // Assert
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.email()).isEqualTo(command.email()),
                () -> assertThat(result.name()).isEqualTo(command.name()),
                () -> assertThat(result.nickName()).isEqualTo(command.nickName()),
                () -> assertThat(result.profileImageUrl()).isNotNull(),
                () -> assertThat(result.role()).isEqualTo(Role.USER)
        );

        then(memberValidator).should(times(1)).validateDuplicateEmail(command.email());
        then(memberRepository).should(times(1)).save(any(Member.class));
    }

    @DisplayName("유저 회원가입 실패 단위테스트 - 중복되는 이메일")
    @Test
    void user_SignUp_Fail() {
        // Arrange
        MemberRegisterCommand command = UserFixture.signUpCommand();
        doThrow(new CustomException(ErrorCode.USER_ALREADY_EXISTS)).when(memberValidator).validateDuplicateEmail(command.email());

        // Act && Assert
        assertThatCode(() -> userSignUpService.userSignUp(command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 존재하는 사용자입니다.");

        then(memberValidator).should(times(1)).validateDuplicateEmail(command.email());
        then(memberRepository).shouldHaveNoInteractions();
    }
}
