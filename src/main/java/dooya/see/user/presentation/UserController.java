package dooya.see.user.presentation;

import dooya.see.auth.domain.LoginUser;
import dooya.see.user.application.dto.*;
import dooya.see.user.application.service.UserQueryService;
import dooya.see.user.application.service.UserSignUpService;
import dooya.see.user.application.service.UserUpdateService;
import dooya.see.user.application.service.UserValidator;
import dooya.see.user.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static dooya.see.user.presentation.dto.UserPresentationMapper.*;

/**
 * {@code UserController} 클래스는 사용자 관련 HTTP 요청을 처리하는
 * 프레젠테이션 계층의 REST 컨트롤러입니다.
 *
 * <p>주요 기능으로는 회원가입, 로그인한 사용자 정보 조회,
 * 이메일 중복 체크, 닉네임 업데이트 등이 포함됩니다.
 *
 * <p>각 메서드는 서비스 계층과 연동하여 비즈니스 로직을 수행하고,
 * 요청과 응답 객체 간 변환은 {@link UserPresentationMapper}를 통해 처리합니다.
 *
 * <p>인증된 사용자의 정보는 Spring Security의 {@link AuthenticationPrincipal}
 * 어노테이션을 통해 주입받습니다.
 *
 * <p>API 경로는 "/api/users"로 시작하며,
 * RESTful 설계 원칙에 따라 자원에 대한 CRUD를 처리합니다.
 *
 * @author dooya
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSignUpService userSignUpService;
    private final UserQueryService userQueryService;
    private final UserValidator userValidator;
    private final UserUpdateService userUpdateService;

    @PostMapping
    public ResponseEntity<UserSignUpResponse> userSignUp(@Valid @RequestBody UserSignUpRequest request) {
        UserSignUpCommand command = toSignCommand(request);
        UserResult result = userSignUpService.userSignUp(command);

        return ResponseEntity.created(URI.create("/api/users/" + result.id())).body(toSignResponse(result));
    }

    @GetMapping
    public ResponseEntity<UserSignUpResponse> getUserByEmail(@AuthenticationPrincipal LoginUser loginUser) {
        String email = loginUser.getUsername();
        UserResult result = userQueryService.getUserByEmail(email);

        return ResponseEntity.ok().body(toSignResponse(result));
    }

    @GetMapping("check-email")
    public ResponseEntity<Void> checkEmailDuplicate(@RequestParam String email) {
        userValidator.validateDuplicateEmail(email);

        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<UserUpdateResponse> updateUserNickName(@AuthenticationPrincipal LoginUser loginUser,
                                                                 @Valid @RequestBody UserUpdateRequest request) {
        String email = loginUser.getUsername();
        UserUpdateCommand command = toUpdateCommand(request);
        UserResult result = userUpdateService.updateNickName(email, command);

        return ResponseEntity.ok().body(toUpdateResponse(result));
    }

    @PutMapping("/password")
    public ResponseEntity<PasswordUpdateResponse> updateUserPassword(@AuthenticationPrincipal LoginUser loginUser,
                                                   @Valid @RequestBody PasswordUpdateRequest request) {
        String email = loginUser.getUsername();
        PasswordUpdateCommand command = toPasswordUpdateCommand(request);
        PasswordUpdateResult result = userUpdateService.updatePassword(email, command);

        return ResponseEntity.ok().body(toPasswordUpdateResponse(result));
    }
}
