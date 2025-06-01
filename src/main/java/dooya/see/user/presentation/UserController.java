package dooya.see.user.presentation;

import dooya.see.auth.domain.LoginUser;
import dooya.see.user.application.dto.*;
import dooya.see.user.application.service.UserQueryService;
import dooya.see.user.application.service.UserSignUpService;
import dooya.see.user.application.service.UserUpdateService;
import dooya.see.user.application.service.UserValidator;
import dooya.see.user.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
@Tag(name = "User", description = "유저 관련 API입니다. 유저 관련된 기능을 제공합니다.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSignUpService userSignUpService;
    private final UserQueryService userQueryService;
    private final UserValidator userValidator;
    private final UserUpdateService userUpdateService;

    @Operation(
            summary = "회원가입",
            description = "사용자가 이메일, 비밀번호, 닉네임을 입력하여 회원가입합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserSignUpResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserSignUpResponse> userSignUp(@Valid @RequestBody UserSignUpRequest request) {
        UserSignUpCommand command = toSignCommand(request);
        UserResult result = userSignUpService.userSignUp(command);

        return ResponseEntity.created(URI.create("/api/users/" + result.id())).body(toSignResponse(result));
    }

    @Operation(
            summary = "로그인된 사용자 정보 조회",
            description = "Access Token에 포함된 이메일을 사용하여 현재 로그인한 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UserResponse> getUserByEmail(@AuthenticationPrincipal LoginUser loginUser) {
        String email = loginUser.getUsername();
        UserResult result = userQueryService.getUserByEmail(email);

        return ResponseEntity.ok().body(toResponse(result));
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "회원가입 시 이메일이 이미 존재하는지 여부를 확인합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용 가능한 이메일", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = @Content)
    })
    @GetMapping("check-email")
    public ResponseEntity<Void> checkEmailDuplicate(@RequestParam String email) {
        userValidator.validateDuplicateEmail(email);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "닉네임 변경",
            description = "로그인한 사용자가 닉네임을 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 변경 성공",
                    content = @Content(schema = @Schema(implementation = NickNameUpdateResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
    })
    @PutMapping("/nick-name")
    public ResponseEntity<NickNameUpdateResponse> updateUserNickName(@AuthenticationPrincipal LoginUser loginUser,
                                                                     @Valid @RequestBody NickNameUpdateRequest request) {
        String email = loginUser.getUsername();
        NickNameUpdateCommand command = toUpdateCommand(request);
        UserResult result = userUpdateService.updateNickName(email, command);

        return ResponseEntity.ok().body(toUpdateResponse(result));
    }

    @Operation(
            summary = "비밀번호 변경",
            description = "기존 비밀번호와 새 비밀번호를 입력하여 비밀번호를 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공",
                    content = @Content(schema = @Schema(implementation = PasswordUpdateResponse.class))),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호가 일치하지 않음", content = @Content)
    })
    @PutMapping("/password")
    public ResponseEntity<PasswordUpdateResponse> updateUserPassword(@AuthenticationPrincipal LoginUser loginUser,
                                                   @Valid @RequestBody PasswordUpdateRequest request) {
        String email = loginUser.getUsername();
        PasswordUpdateCommand command = toPasswordUpdateCommand(request);
        PasswordUpdateResult result = userUpdateService.updatePassword(email, command);

        return ResponseEntity.ok().body(toPasswordUpdateResponse(result));
    }

    @Operation(
            summary = "프로필 이미지 변경",
            description = "사용자가 새로운 프로필 이미지를 업로드합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 이미지 변경 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "파일 업로드 실패 또는 형식 오류", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음", content = @Content)
    })
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateProfileImage(@AuthenticationPrincipal LoginUser loginUser,
                                                           @RequestPart MultipartFile profileImage) {
        String email = loginUser.getUsername();
        UserResult result = userUpdateService.updateProfileImage(email, profileImage);

        return ResponseEntity.ok().body(toResponse(result));
    }
}
