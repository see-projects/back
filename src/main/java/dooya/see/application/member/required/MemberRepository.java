package dooya.see.application.member.required;


import dooya.see.domain.member.Member;
import dooya.see.domain.member.Profile;
import dooya.see.domain.shared.Email;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * 회원 관련 데이터를 처리하는 레포지토리 인터페이스
 */
public interface MemberRepository extends Repository<Member, Long> {
    /**
     * 이메일을 기준으로 회원 정보를 조회합니다.
     *
     * @param email 조회할 회원의 이메일 정보
     * @return 이메일에 해당하는 회원 정보가 포함된 Optional 객체 (찾지 못하면 Optional.empty() 반환)
     */
    Optional<Member> findByEmail(Email email);

    /**
     * 회원 정보를 저장합니다.
     *
     * @param member 저장할 회원 정보
     * @return 저장된 회원 정보
     */
    Member save(Member member);

    /**
     * ID를 기준으로 회원 정보를 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return ID에 해당하는 회원 정보가 포함된 Optional 객체 (찾지 못하면 Optional.empty() 반환)
     */
    Optional<Member> findById(Long memberId);

    /**
     * 프로필 정보를 기준으로 회원 정보를 조회합니다.
     *
     * @param profile 조회할 회원의 프로필 정보
     * @return 프로필에 해당하는 회원 정보가 포함된 Optional 객체 (찾지 못하면 Optional.empty() 반환)
     */
    @Query("select m from Member m where m.detail.profile = :profile")
    Optional<Member> findByProfile(Profile profile);
}
