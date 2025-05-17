package dooya.see.user.presentation;

import dooya.see.auth.domain.LoginUser;
import dooya.see.user.application.UserQueryService;
import dooya.see.user.application.UserSignUpCommand;
import dooya.see.user.application.UserSignUpService;
import dooya.see.user.application.UserValidator;
import dooya.see.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserSignUpService userSignUpService;
    private final UserQueryService userQueryService;
    private final UserValidator userValidator;

    @PostMapping
    public ResponseEntity<UserSignUpResponse> userSignUp(@Valid @RequestBody UserSignUpRequest request) {
        UserSignUpCommand command = UserDtoMapper.toCommand(request);
        User user = userSignUpService.userSignUp(command);
        UserSignUpResponse response = UserDtoMapper.toResponse(user);

        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<UserSignUpResponse> getUserByEmail(@AuthenticationPrincipal LoginUser loginUser) {
        String email = loginUser.getUsername();
        User user = userQueryService.getUserByEmail(email);
        UserSignUpResponse response = UserDtoMapper.toResponse(user);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("check-email")
    public ResponseEntity<Void> checkEmailDuplicate(@RequestParam String email) {
        userValidator.validateDuplicateEmail(email);
        return ResponseEntity.ok().build();
    }
}
