package dooya.see.domain.member;

public record LoginResult(
        Member member,
        String accessToken
) {
}
