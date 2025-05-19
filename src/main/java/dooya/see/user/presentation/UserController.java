package dooya.see.user.presentation;

import dooya.see.auth.domain.LoginUser;
import dooya.see.user.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static dooya.see.user.presentation.UserPresentationMapper.*;

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
}
