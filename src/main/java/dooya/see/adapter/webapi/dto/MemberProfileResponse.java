package dooya.see.adapter.webapi.dto;

import dooya.see.domain.member.Member;

public record MemberProfileResponse(
        Long memberId,
        String email,
        String nickname,
        String profile
) {
    public static MemberProfileResponse from(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getEmail().address(),
                member.getNickname(),
                member.getDetail().getProfile() != null ? 
                    member.getDetail().getProfile().address() : null
        );
    }
}
