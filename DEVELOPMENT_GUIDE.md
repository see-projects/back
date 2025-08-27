# See 개발 가이드

이 문서는 See 프로젝트의 개발 철학, 아키텍처 원칙, 그리고 실무 가이드라인을 제공합니다.

## 🎯 개발 철학

### 핵심 가치
1. **클린 코드 우선**: 동작하면서도 깨끗한 코드를 추구
2. **도메인 중심 설계**: 비즈니스 로직이 기술에 침범당하지 않도록 보호
3. **테스트 주도 개발**: 모든 코드는 테스트와 함께
4. **지속적 개선**: 작은 단위의 점진적 개선을 통한 발전

### 개발 원칙
- **SOLID 원칙** 준수
- **DRY (Don't Repeat Yourself)** - 중복 제거
- **YAGNI (You Aren't Gonna Need It)** - 과도한 설계 지양
- **Tell, Don't Ask** - 객체의 협력과 책임 중시

## 🏗️ 아키텍처 가이드

### 헥사고날 아키텍처 실천

#### 패키지 구조 규칙
```
src/main/java/dooya/see/
├── domain/                 # 도메인 계층
│   ├── {aggregate}/        # 애그리거트별 패키지
│   │   ├── {Entity}.java   # 엔티티 클래스
│   │   ├── {ValueObject}.java  # 값 객체
│   │   └── {Exception}.java    # 도메인 예외
│   └── shared/            # 공유 도메인 객체
├── application/           # 애플리케이션 계층
│   └── {aggregate}/
│       ├── provided/      # Primary Port (Inbound)
│       ├── required/      # Secondary Port (Outbound)
│       └── {Service}.java # 애플리케이션 서비스
└── adapter/              # 어댑터 계층
    ├── webapi/           # REST API
    ├── persistence/      # 데이터베이스
    ├── security/         # 보안
    └── integration/      # 외부 연동
```

#### 의존성 규칙
1. **Domain**: 외부 의존성 없음 (순수 Java)
2. **Application**: Domain만 의존
3. **Adapter**: Application과 Domain 의존

#### 포트와 어댑터 설계

##### Primary Port (제공 인터페이스)
```java
// 애플리케이션이 외부에 제공하는 기능
public interface MemberRegister {
    Member register(MemberRegisterRequest request);
    Member activate(Long memberId);
}
```

##### Secondary Port (필요 인터페이스)
```java
// 애플리케이션이 외부 시스템에 필요로 하는 기능
public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByEmail(Email email);
}
```

## 🧪 테스트 전략

### 테스트 피라미드

#### 1. 단위 테스트 (Unit Tests)
**도메인 로직 테스트**에 집중합니다.

```java
@Test
void registerMember_ValidRequest_CreatesPendingMember() {
    // given
    MemberRegisterRequest request = createValidRegisterRequest();
    PasswordEncoder encoder = createPasswordEncoder();
    
    // when
    Member member = Member.register(request, encoder);
    
    // then
    assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
    assertThat(member.getEmail()).isEqualTo(request.email());
    assertThat(member.getNickname()).isEqualTo(request.nickname());
}
```

#### 2. 통합 테스트 (Integration Tests)
**애플리케이션 서비스**의 전체 플로우를 검증합니다.

```java
@SpringBootTest
@Transactional
class MemberModifyServiceTest {
    
    @Autowired
    private MemberRegister memberRegister;
    
    @Test
    void register_ValidRequest_SavesMemberSuccessfully() {
        // given
        MemberRegisterRequest request = createValidRegisterRequest();
        
        // when
        Member member = memberRegister.register(request);
        
        // then
        assertThat(member.getId()).isNotNull();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
    }
}
```

### TDD 프로세스

#### Red-Green-Refactor 사이클
1. **RED**: 실패하는 테스트 작성
2. **GREEN**: 테스트를 통과하는 최소한의 코드 작성
3. **REFACTOR**: 코드 개선 (중복 제거, 가독성 향상)

## 🔧 코딩 컨벤션

### 네이밍 규칙

#### 패키지명
- **소문자** 사용: `dooya.see.domain.member`
- **명사형** 사용: `member`, `post`, `comment`
- **복수형 지양**: `member` (O), `members` (X)

#### 클래스명
- **PascalCase** 사용: `Member`, `PostContent`
- **명사** 사용: 엔티티, 값 객체
- **형용사 + 명사**: 예외 클래스는 `DuplicateEmailException`

#### 메서드명
- **camelCase** 사용: `register`, `changePassword`
- **동사** 사용: `create`, `update`, `delete`
- **boolean 반환**: `is`, `has`, `can` 접두사 사용

### 코드 스타일

#### 메서드 길이
```java
// 좋은 예: 한 가지 일만 하는 짧은 메서드
public Member register(MemberRegisterRequest request, PasswordEncoder encoder) {
    validateDuplicateEmail(request.email());
    validateDuplicateNickname(request.nickname());
    return Member.register(request, encoder);
}
```

#### 생성자 주입
```java
// 좋은 예: final 필드와 생성자 주입
@Service
@RequiredArgsConstructor
public class MemberModifyService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
}
```

---

이 개발 가이드는 See 프로젝트의 **일관성 있는 개발 경험**을 제공하고, **품질 높은 코드**를 생산하는 것을 목표로 합니다.
