package dooya.see.user.presentation;

import dooya.see.auth.domain.LoginUser;
import dooya.see.user.application.*;
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
    private final UserUpdateService userUpdateService;

    @PostMapping
    public ResponseEntity<UserSignUpResponse> userSignUp(@Valid @RequestBody UserSignUpRequest request) {
        UserSignUpCommand command = UserDtoMapper.toSignCommand(request);
        User user = userSignUpService.userSignUp(command);

        UserSignUpResponse response = UserDtoMapper.toSignResponse(user);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<UserSignUpResponse> getUserByEmail(@AuthenticationPrincipal LoginUser loginUser) {
        String email = loginUser.getUsername();
        User user = userQueryService.getUserByEmail(email);

        UserSignUpResponse response = UserDtoMapper.toSignResponse(user);
        return ResponseEntity.ok().body(response);
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
        UserUpdateCommand command = UserDtoMapper.toUpdateCommand(request);

        User user = userUpdateService.updateNickName(email, command);

        UserUpdateResponse response = UserDtoMapper.toUpdateResponse(user);
        return ResponseEntity.ok().body(response);
    }
}
