package dooya.see.admin.presentation;

import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.service.UserQueryService;
import dooya.see.user.presentation.dto.UserPresentationMapper;
import dooya.see.user.presentation.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin", description = "관리자 전용 API입니다. 관리자 권한으로 접근 가능한 유저 관리 기능을 제공합니다.")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserQueryService userQueryService;

    @Operation(
            summary = "전체 사용자 목록 조회",
            description = "관리자가 모든 사용자 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 토큰 없음", content = @Content),
            @ApiResponse(responseCode = "403", description = "인가 실패 - 관리자 권한 없음", content = @Content),
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResult> users = userQueryService.getUsers();

        return ResponseEntity.ok(UserPresentationMapper.toResponses(users));
    }
}
