package dooya.see.admin.presentation;

import dooya.see.user.application.dto.UserResult;
import dooya.see.user.application.service.UserQueryService;
import dooya.see.user.presentation.dto.UserPresentationMapper;
import dooya.see.user.presentation.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserQueryService userQueryService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResult> users = userQueryService.getUsers();

        return ResponseEntity.ok(UserPresentationMapper.toResponses(users));
    }
}
