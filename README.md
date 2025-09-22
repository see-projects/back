# See - 헥사고날 아키텍처 기반 커뮤니티 플랫폼

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Test Coverage](https://img.shields.io/badge/Test%20Coverage-95%25-brightgreen)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-purple)

**See**는 회원 인증과 소셜 커뮤니티 기능을 제공하는 현대적인 웹 애플리케이션입니다. 헥사고날 아키텍처와 도메인 주도 설계(DDD) 원칙을 적용하여 확장 가능하고 유지보수하기 쉬운 구조로 설계되었습니다.

## 📚 관련 문서

- **[도메인 모델 문서](docs/domain-model.md)**: 비즈니스 로직과 도메인 설계에 대한 상세 설명
- **[용어 사전](docs/glossary.md)**: 프로젝트에서 사용되는 유비쿼터스 언어 정의

## 주요 기능

### 회원 관리
- **회원 가입/로그인**: JWT 토큰 기반 인증 시스템
- **프로필 관리**: 닉네임, 자기소개, 프로필 주소 설정
- **계정 상태 관리**: 활성화/비활성화 상태 전환
- **보안**: BCrypt 암호화로 비밀번호 안전 보관

### 포스트 시스템
- **포스트 작성/수정/삭제**: 풍부한 콘텐츠 작성 도구
- **상태 관리**: 초안(DRAFT) → 발행(PUBLISHED) → 숨김(HIDDEN) → 삭제(DELETED)
- **카테고리 분류**: TECH, LIFE, TRAVEL, FOOD, HOBBY 카테고리
- **실시간 통계**: 조회수, 좋아요 수, 댓글 수 추적
- **도메인 이벤트**: 비즈니스 이벤트 기반 사이드 이펙트 처리

### 댓글 시스템
- **계층형 댓글**: 대댓글 지원으로 깊이 있는 토론
- **실시간 소통**: 즉시 반영되는 댓글 시스템
- **권한 관리**: 작성자만 수정/삭제 가능
- **상태 관리**: 활성/숨김/삭제 상태 지원

### 상호작용 시스템
- **좋아요**: 포스트에 대한 사용자 선호도 표현
- **통계 관리**: 이벤트 기반 비동기 통계 업데이트
- **중복 방지**: 동일 사용자의 중복 좋아요 차단

## 아키텍처

### 헥사고날 아키텍처 (Ports & Adapters)

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Web API       │────▶│   Application   │────▶│     Domain      │
│   (Adapter)     │     │  (Use Cases)    │     │  (Aggregates)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Security      │     │   Persistence   │     │ Domain Events   │
│   (Adapter)     │     │   (Adapter)     │     │ (Event Handler) │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 계층별 책임

- **Domain Layer**: 비즈니스 규칙과 도메인 로직
    - 애그리거트 루트: Member, Post, Comment, PostLike, PostStats
    - 값 객체: Email, Profile, PostContent, CommentContent
    - 도메인 이벤트: PostCreated, PostViewed, PostLiked 등

- **Application Layer**: 유스케이스 조율과 트랜잭션 관리
    - Primary Port: MemberManager, PostManager, CommentManager
    - Secondary Port: MemberRepository, PostRepository, PostStatsRepository
    - 이벤트 핸들러: PostStatsEventHandler

- **Adapter Layer**: 외부 시스템과의 연동
    - Web API: REST 엔드포인트
    - Security: JWT 인증/인가
    - Persistence: JPA 구현체

## 기술 스택

### Core Framework
- **Java 21**: 최신 LTS 버전으로 성능과 보안 강화
- **Spring Boot 3.5.4**: 최신 스프링 부트로 개발 생산성 향상
- **Spring Data JPA**: 데이터 접근 계층 추상화
- **Spring Security**: 인증/인가 보안 체계

### Database
- **MySQL 8.0**: 운영 환경 데이터베이스
- **H2**: 개발/테스트 환경 인메모리 데이터베이스
- **Docker Compose**: 컨테이너 기반 개발 환경

### Testing & Quality
- **JUnit 5**: 현대적인 테스트 프레임워크
- **AssertJ**: 가독성 높은 테스트 어서션
- **ArchUnit**: 아키텍처 규칙 자동 검증
- **95% 테스트 커버리지**: TDD 기반 높은 코드 품질
- **JaCoCo**: 테스트 커버리지 측정

### Development Tools
- **Lombok**: 보일러플레이트 코드 자동 생성
- **Docker**: 일관된 개발/운영 환경
- **GitHub Actions**: CI/CD 자동화

## 빠른 시작

### 사전 요구사항
- Java 21 이상
- Docker & Docker Compose
- IDE (IntelliJ IDEA 권장)

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-repo/see.git
cd see
```

### 2. 데이터베이스 시작
```bash
docker-compose up -d mysql
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 4. API 테스트
```bash
# 회원 가입
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "nickname": "테스터",
    "password": "password123",
    "profileAddress": "tester"
  }'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# 포스트 작성
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "title": "첫 번째 포스트",
    "body": "헥사고날 아키텍처에 대한 글입니다.",
    "category": "TECH"
  }'
```

## 프로젝트 구조

```
src/
├── main/java/dooya/see/
│   ├── domain/                 # 도메인 계층
│   │   ├── member/             # 회원 애그리거트
│   │   │   ├── Member.java
│   │   │   ├── MemberDetail.java
│   │   │   ├── Profile.java
│   │   │   └── Email.java
│   │   ├── post/               # 포스트 애그리거트  
│   │   │   ├── Post.java
│   │   │   ├── PostContent.java
│   │   │   ├── PostMetaData.java
│   │   │   ├── PostStats.java
│   │   │   └── event/          # 도메인 이벤트
│   │   ├── comment/            # 댓글 애그리거트
│   │   ├── postlike/           # 좋아요 애그리거트
│   │   └── shared/             # 공유 도메인 객체
│   ├── application/            # 애플리케이션 계층
│   │   ├── member/             
│   │   │   ├── provided/       # Primary Port
│   │   │   └── required/       # Secondary Port  
│   │   ├── post/
│   │   │   ├── PostStatsEventHandler.java
│   │   │   ├── provided/
│   │   │   └── required/
│   │   └── comment/
│   └── adapter/                # 어댑터 계층
│       ├── webapi/             # REST API
│       ├── security/           # 보안
│       └── persistence/        # 데이터베이스
└── test/                       # 테스트 코드
    ├── domain/                 # 단위 테스트 (순수 Java)
    ├── application/            # 통합 테스트
    └── adapter/                # API 테스트 (E2E)
```

## 테스트

### 테스트 전략
- **단위 테스트**: 도메인 로직을 순수 Java로 빠르게 검증
- **통합 테스트**: 포트 구현체를 모의 객체로 대체하여 애플리케이션 서비스 검증
- **인수 테스트**: 실제 어댑터를 사용하여 End-to-End 시나리오 검증

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 테스트 커버리지 확인  
./gradlew jacocoTestReport
open build/jacocoHtml/index.html

# 아키텍처 규칙 검증
./gradlew test --tests "*ArchUnitTest"
```

## 주요 지표

- **테스트 커버리지**: 95% (JaCoCo 측정)
- **아키텍처 준수율**: 100% (ArchUnit으로 자동 검증)
- **빌드 시간**: 평균 2분 이하
- **API 응답시간**: 평균 200ms 이하

## 개발 원칙

### 1. 도메인 주도 설계 (DDD)
- **유비쿼터스 언어**: 비즈니스 용어를 코드에 직접 반영
- **애그리거트 패턴**: 트랜잭션과 일관성 경계 명확히 설정
- **도메인 이벤트**: 애그리거트 간 느슨한 결합 유지

### 2. 헥사고날 아키텍처
- **의존성 역전**: 모든 의존성이 도메인 중심으로 향함
- **포트와 어댑터**: 인터페이스 기반 확장 가능한 설계
- **관심사 분리**: 비즈니스 로직과 기술적 세부사항 완전 분리

### 3. 테스트 주도 개발 (TDD)
- **Red-Green-Refactor**: 실패하는 테스트부터 시작
- **Given-When-Then**: 모든 테스트를 일관된 구조로 작성
- **높은 커버리지**: 95% 테스트 커버리지로 안전한 리팩토링 보장

## 도메인 이벤트 시스템

### 이벤트 처리 방식
```java
// 이벤트 발행 (도메인 계층)
public void publish() {
    this.status = PostStatus.PUBLISHED;
    this.addDomainEvent(new PostPublished(this.getId(), this.memberId));
}

// 이벤트 처리 (애플리케이션 계층)
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handlePostViewed(PostViewed event) {
    try {
        postStatsManager.incrementViewCount(event.postId());
    } catch (Exception e) {
        // 사이드 이펙트 실패가 메인 로직에 영향 없도록 격리
    }
}
```

### 이벤트 종류
- **PostCreated**: 포스트 생성 시 통계 초기화
- **PostViewed**: 조회 시 조회수 증가
- **PostLiked/PostUnliked**: 좋아요/취소 시 좋아요 수 증감
- **CommentCreated/CommentDeleted**: 댓글 생성/삭제 시 댓글 수 증감

## 보안

- **JWT 토큰**: Stateless 인증 방식
- **BCrypt 암호화**: 강력한 비밀번호 해싱
- **CORS 설정**: 안전한 크로스 도메인 요청
- **입력 검증**: 도메인 객체에서 비즈니스 규칙 검증
- **권한 관리**: 리소스 소유자만 수정/삭제 가능

## CI/CD

### GitHub Actions
- **자동화된 빌드**: 모든 푸시/PR에서 자동 빌드
- **품질 게이트**:
    - 모든 테스트 통과
    - 95% 이상 테스트 커버리지
    - ArchUnit 아키텍처 규칙 준수
- **Docker 지원**: 컨테이너화된 배포

### 개발 워크플로우
```bash
# 1단계: TDD로 비즈니스 로직 개발
# 2단계: ArchUnit으로 아키텍처 검증  
# 3단계: 통합 테스트로 전체 플로우 검증
# 4단계: 코드 리뷰 후 머지
```

## 성능 최적화

### 애그리거트 분리
- **PostStats 설계**: Post 애그리거트 내의 별도 엔티티로 통계 전용 처리
- **이벤트 기반 업데이트**: 통계는 비동기로 최종 일관성 보장
- **트랜잭션 분리**: 통계 실패가 본 작업에 영향 없음

### 데이터베이스 최적화
- **적절한 인덱싱**: 조회 성능 최적화
- **연관관계 최적화**: N+1 문제 방지
- **커넥션 풀**: HikariCP로 커넥션 관리

## 확장 계획

### Phase 1 (현재)
- ✅ 기본 CRUD 기능
- ✅ 헥사고날 아키텍처 적용
- ✅ 도메인 이벤트 시스템
- ✅ 95% 테스트 커버리지

### Phase 2 (예정)
- 📋 태그 시스템
- 📋 팔로우 기능
- 📋 알림 시스템
- 📋 검색 기능

### Phase 3 (장기)
- 📋 CQRS 패턴 도입
- 📋 마이크로서비스 분리
- 📋 실시간 기능 (WebSocket)

## 기여 가이드라인

1. **아키텍처 원칙 준수**: ArchUnit 테스트 통과 필수
2. **테스트 작성**: 모든 기능에 대한 테스트 코드 작성
3. **커버리지 유지**: 95% 이상 테스트 커버리지 유지
4. **코드 리뷰**: 모든 변경사항은 코드 리뷰 후 머지
5. **커밋 컨벤션**: Conventional Commits 규칙 준수

---

**See 프로젝트**는 현대적인 백엔드 개발 패러다임을 실무에 적용한 레퍼런스 프로젝트입니다. 헥사고날 아키텍처, DDD, TDD의 조합으로 확장 가능하고 유지보수하기 쉬운 고품질 소프트웨어를 추구합니다.