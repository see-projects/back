# See - 헥사고날 아키텍처 기반 커뮤니티 플랫폼

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Test Coverage](https://img.shields.io/badge/Test%20Coverage-%3E80%25-brightgreen)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-purple)

**See**는 회원 인증과 소셜 커뮤니티 기능을 제공하는 현대적인 웹 애플리케이션입니다. 헥사고날 아키텍처와 도메인 모델 패턴 원칙을 적용하여 확장 가능하고 유지보수하기 쉬운 구조로 설계되었습니다.

## 🌟 주요 기능

### 👤 회원 관리
- **회원 가입/로그인**: JWT 토큰 기반 인증 시스템
- **프로필 관리**: 닉네임, 자기소개 등 개인정보 수정
- **계정 상태 관리**: 활성화/비활성화 상태 전환
- **보안**: BCrypt 암호화로 비밀번호 안전 보관

### 📝 포스트 시스템
- **포스트 작성/수정/삭제**: 풍부한 콘텐츠 작성 도구
- **상태 관리**: 초안(DRAFT) → 발행(PUBLISHED) → 숨김(HIDDEN) → 삭제(DELETED)
- **카테고리 분류**: 체계적인 콘텐츠 분류
- **조회수 추적**: 실시간 조회 통계
- **좋아요 시스템**: 사용자 참여도 측정

### 💬 댓글 시스템
- **계층형 댓글**: 대댓글 지원으로 토론 가능
- **실시간 소통**: 즉시 반영되는 댓글 시스템
- **권한 관리**: 작성자만 수정/삭제 가능
- **상태 관리**: 활성/숨김/삭제 상태 지원

## 🏗️ 아키텍처

### 헥사고날 아키텍처 (Ports & Adapters)

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Web API       │────▶│   Application   │────▶│     Domain      │
│   (Adapter)     │     │    (Use Case)   │     │  (Business)     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                        │                        
         ▼                        ▼                        
┌─────────────────┐     ┌─────────────────┐               
│   Security      │     │   Persistence   │               
│   (Adapter)     │     │   (Adapter)     │               
└─────────────────┘     └─────────────────┘               
```

### 계층별 책임

- **🎯 Domain Layer**: 비즈니스 규칙과 도메인 로직
- **🔄 Application Layer**: 유스케이스 조율과 트랜잭션 관리
- **🔌 Adapter Layer**: 외부 시스템과의 연동 (Web API, DB, Security)

## 🛠️ 기술 스택

### Core Framework
- **Java 21**: 최신 LTS 버전으로 성능과 보안 강화
- **Spring Boot 3.5.4**: 최신 스프링 부트로 개발 생산성 향상
- **Spring Data JPA**: 데이터 접근 계층 추상화
- **Spring Security Core**: 인증/인가 보안 체계

### Database
- **MySQL 8.0**: 운영 환경 데이터베이스
- **H2**: 개발/테스트 환경 인메모리 데이터베이스
- **Docker Compose**: 컨테이너 기반 개발 환경

### Testing
- **JUnit 5**: 현대적인 테스트 프레임워크
- **AssertJ**: 가독성 높은 테스트 어서션
- **ArchUnit**: 아키텍처 규칙 검증
- **80%+ 테스트 커버리지**: 높은 코드 품질 보장

### Development Tools
- **Lombok**: 보일러플레이트 코드 자동 생성
- **SpotBugs**: 정적 분석으로 잠재 버그 탐지
- **JaCoCo**: 테스트 커버리지 측정
- **Docker**: 일관된 개발/운영 환경

## 🚀 빠른 시작

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
docker-compose up -d
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
    "password": "password123"
  }'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

## 📁 프로젝트 구조

```
src/
├── main/java/dooya/see/
│   ├── 📦 domain/              # 도메인 계층
│   │   ├── member/             # 회원 애그리거트
│   │   ├── post/               # 포스트 애그리거트
│   │   └── shared/             # 공유 도메인 객체
│   ├── 🔄 application/         # 애플리케이션 계층
│   │   ├── member/             
│   │   │   ├── provided/       # Primary Port
│   │   │   └── required/       # Secondary Port
│   │   └── post/
│   │       ├── provided/
│   │       └── required/
│   └── 🔌 adapter/             # 어댑터 계층
│       ├── webapi/             # REST API
│       ├── security/           # 보안
│       └── persistence/        # 데이터베이스
└── test/                       # 테스트 코드
    ├── domain/                 # 단위 테스트
    ├── application/            # 통합 테스트
    └── adapter/                # API 테스트
```

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 테스트 커버리지 확인
```bash
./gradlew jacocoTestReport
open build/jacocoHtml/index.html
```

### 아키텍처 규칙 검증
```bash
./gradlew test --tests "*HexagonalArchitectureTest"
```

## 📊 주요 지표

- **테스트 커버리지**: 80% 이상 유지
- **아키텍처 준수율**: 100% (ArchUnit으로 검증)
- **코드 품질**: SpotBugs 정적 분석 통과
- **성능**: 평균 응답시간 200ms 이하

## 🎯 개발 원칙

### 1. 도메인 주도 설계 (DDD)
- **유비쿼터스 언어**: 비즈니스 용어를 코드에 직접 반영
- **애그리거트**: 데이터 일관성 경계 명확히 설정
- **도메인 이벤트**: 애그리거트 간 느슨한 결합 유지

### 2. 헥사고날 아키텍처
- **의존성 역전**: 모든 의존성이 도메인을 향함
- **포트와 어댁터**: 인터페이스 기반 확장 가능한 설계
- **관심사 분리**: 비즈니스 로직과 기술적 세부사항 분리

### 3. 테스트 주도 개발 (TDD)
- **Red-Green-Refactor**: 실패하는 테스트부터 시작
- **계층별 테스트**: 단위/통합/E2E 테스트 균형
- **높은 커버리지**: 80% 이상 테스트 커버리지 유지

## 🔐 보안

- **JWT 토큰**: Stateless 인증 방식
- **BCrypt 암호화**: 강력한 비밀번호 해싱
- **CORS 설정**: 안전한 크로스 도메인 요청
- **입력 검증**: Jakarta Validation으로 데이터 무결성 보장

## 🚦 CI/CD

- **GitHub Actions**: 자동화된 빌드/테스트
- **품질 게이트**: 테스트 통과 + 커버리지 80% 이상
- **Docker 지원**: 일관된 배포 환경
- **Health Check**: 애플리케이션 상태 모니터링

### 개발 가이드라인
- 모든 코드는 테스트와 함께 작성
- 아키텍처 규칙 준수 (ArchUnit 테스트 통과)
- 코드 리뷰 후 머지
- 커밋 메시지 규칙 준수