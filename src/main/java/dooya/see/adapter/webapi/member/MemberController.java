package dooya.see.adapter.webapi.member;

import dooya.see.adapter.webapi.member.dto.MemberRegisterResponse;
import dooya.see.adapter.webapi.member.dto.NickNameUpdateResponse;
import dooya.see.adapter.webapi.member.dto.PasswordUpdateResponse;
import dooya.see.domain.auth.LoginUser;
import dooya.see.domain.member.*;
import dooya.see.application.member.provided.MemberFinder;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.application.member.provided.MemberUpdateService;
import dooya.see.application.member.provided.MemberValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

import static dooya.see.domain.member.MemberPresentationMapper.*;


@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberRegister memberRegister;
    private final MemberFinder memberQueryService;
    private final MemberValidator memberValidator;
    private final MemberUpdateService memberUpdateService;

    @PostMapping
    public MemberRegisterResponse userRegister(@Valid @RequestBody MemberRegisterRequest request) {
        Member member = memberRegister.register(request);

        return ResponseEntity.created(URI.create("/api/members/" + result.id())).body(toSignResponse(result));
    }

    @GetMapping
    public ResponseEntity<MemberResponse> getUserByEmail(@AuthenticationPrincipal LoginUser loginUser) {
        String email = loginUser.getUsername();
        MemberResult result = memberQueryService.find(email);

        return ResponseEntity.ok().body(toResponse(result));
    }

    @GetMapping("check-email")
    public ResponseEntity<Void> checkEmailDuplicate(@RequestParam String email) {
        memberValidator.validateDuplicateEmail(email);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/nick-name")
    public ResponseEntity<NickNameUpdateResponse> updateUserNickName(@AuthenticationPrincipal LoginUser loginUser,
                                                                     @Valid @RequestBody MemberInfoUpdateRequest request) {
        String email = loginUser.getUsername();
        NickNameUpdateCommand command = toUpdateCommand(request);
        MemberResult result = memberUpdateService.updateNickName(email, command);

        return ResponseEntity.ok().body(toUpdateResponse(result));
    }

    @PutMapping("/password")
    public ResponseEntity<PasswordUpdateResponse> updateUserPassword(@AuthenticationPrincipal LoginUser loginUser,
                                                   @Valid @RequestBody PasswordUpdateRequest request) {
        String email = loginUser.getUsername();
        PasswordUpdateCommand command = toPasswordUpdateCommand(request);
        PasswordUpdateResult result = memberUpdateService.updatePassword(email, command);

        return ResponseEntity.ok().body(toPasswordUpdateResponse(result));
    }

    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberResponse> updateProfileImage(@AuthenticationPrincipal LoginUser loginUser,
                                                             @RequestPart MultipartFile profileImage) {
        String email = loginUser.getUsername();
        MemberResult result = memberUpdateService.updateProfileImage(email, profileImage);

        return ResponseEntity.ok().body(toResponse(result));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberResponse> updateProfile(@AuthenticationPrincipal LoginUser loginUser,
                                                        @RequestPart(required = false) MemberInfoUpdateRequest request,
                                                        @RequestPart(required = false) MultipartFile profileImage) {
        String email = loginUser.getUsername();
        NickNameUpdateCommand command = MemberPresentationMapper.toUpdateCommand(request);
        MemberResult result = memberUpdateService.updateProfile(email, command, profileImage);

        return ResponseEntity.ok().body(toResponse(result));
    }
}
