package dooya.see.adapter.webapi;

import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.MemberProfileResponse;
import dooya.see.adapter.webapi.dto.MemberRegisterResponse;
import dooya.see.application.member.provided.MemberAuth;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.*;
import dooya.see.domain.member.dto.MemberAuthRequest;
import dooya.see.domain.member.dto.MemberInfoUpdateRequest;
import dooya.see.domain.member.dto.MemberRegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

/**
 * 회원 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class MemberApi {
    private final MemberRegister memberRegister;
    private final MemberAuth memberAuth;

    @PostMapping("/api/members")
    public MemberRegisterResponse register(@RequestBody @Valid MemberRegisterRequest request) {
        Member member = memberRegister.register(request);

        return MemberRegisterResponse.of(member);
    }

    @PostMapping("/api/members/login")
    public MemberAuthResponse login(@RequestBody @Valid MemberAuthRequest request) {
        LoginResult result = memberAuth.login(request);

        return MemberAuthResponse.from(result);
    }

    @GetMapping("/api/members/me")
    public MemberProfileResponse getCurrentMember(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Member member = getCurrentMemberFromToken(token);

        return MemberProfileResponse.from(member);
    }

    @PatchMapping("/api/members/my/deactivate")
    public void deactivate(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        Member member = getCurrentMemberFromToken(token);

        memberRegister.deactivate(member.getId());
    }

    @PutMapping("/api/members/me")
    public MemberProfileResponse updateProfile(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                               @RequestBody @Valid MemberInfoUpdateRequest request) {
        Long currentMemberId = getCurrentMemberIdFromToken(token);
        Member updatedMember = memberRegister.updateInfo(currentMemberId, request);

        return MemberProfileResponse.from(updatedMember);
    }

    // Authentication 관련 메서드
    private String extractTokenFromHeader(String token) {
        return AuthTokenExtractor.extractToken(token);
    }

    private Member getCurrentMemberFromToken(String token) {
        String extractedToken = extractTokenFromHeader(token);
        return memberAuth.getCurrentMember(extractedToken);
    }

    private Long getCurrentMemberIdFromToken(String token) {
        Member currentMember = getCurrentMemberFromToken(token);
        return currentMember.getId();
    }
}
