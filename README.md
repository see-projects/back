# See - 커뮤니티 플랫폼

See는 사용자들이 다양한 콘텐츠를 작성하고 공유하며 활발하게 소통하는 온라인 커뮤니티 플랫폼입니다.

## 🎯 프로젝트 비전

- **콘텐츠 중심 생태계**: 사용자가 만드는 가치 있는 콘텐츠
- **소통과 공유**: 커뮤니티 구성원 간의 활발한 상호작용  
- **개방형 플랫폼**: 누구나 참여하고 기여할 수 있는 환경

## 🏗️ 아키텍처 원칙

이 프로젝트는 **토비 스타일 클린 스프링** 철학을 바탕으로 다음 원칙들을 적용합니다:

- **헥사고날 아키텍처**: 포트와 어댑터로 관심사 분리
- **도메인 중심 설계**: 비즈니스 로직이 도메인에 집중
- **의존성 역전**: 모든 의존성이 도메인을 향함
- **테스트 주도 개발**: 모든 코드는 테스트와 함께

## 🚀 빠른 시작

### 필수 요구사항
- Java 21
- Docker & Docker Compose

### 로컬 개발 환경 실행

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd see
   ```

2. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```
   
   Spring Boot Docker Compose 지원으로 MySQL 컨테이너가 자동으로 실행됩니다.

3. **애플리케이션 접속**
   - API 서버: http://localhost:8080
   - H2 콘솔: http://localhost:8080/h2-console (개발 전용)

### Docker Compose 수동 관리

필요한 경우 Docker Compose를 수동으로 관리할 수 있습니다:

```bash
# 컨테이너 시작
docker-compose up -d

# 컨테이너 중지
docker-compose down

# 컨테이너 및 볼륨 완전 삭제
docker-compose down -v
```

## 🗄️ 데이터베이스 정보

### 개발 환경
- **Type**: H2 In-Memory Database
- **URL**: jdbc:h2:mem:testdb
- **Console**: http://localhost:8080/h2-console

### 운영 환경 (Docker Compose)
- **Host**: localhost:3306
- **Database**: see_dev
- **Username**: see_user
- **Password**: see_password

## 📚 기술 스택

### 백엔드 핵심
- **Java 21**: 최신 LTS 버전
- **Spring Boot 3.5.4**: 최신 Spring Boot
- **Spring Data JPA**: 데이터 접근 계층
- **Spring Web**: REST API 구현
- **Spring Security Core**: 보안 및 암호화

### 보안
- **JWT**: JSON Web Token 기반 인증
- **BCrypt**: 비밀번호 해싱

### 데이터베이스
- **H2**: 개발 및 테스트용 인메모리 DB
- **MySQL 8.0**: 운영 데이터베이스

### 테스트
- **JUnit 5**: 테스트 프레임워크
- **AssertJ**: 플루언트 어서션
- **Mockito**: 모킹 프레임워크
- **ArchUnit**: 아키텍처 테스트
- **JUnit Pioneer**: 확장 어노테이션

### 개발 도구
- **Lombok**: 보일러플레이트 코드 제거
- **SpotBugs**: 정적 분석 도구
- **Docker Compose**: 컨테이너 기반 개발 환경

## 🏗️ 프로젝트 구조

```
src/main/java/dooya/see/
├── domain/                 # 도메인 계층 (순수 비즈니스 로직)
│   ├── member/            # 회원 애그리거트
│   ├── post/              # 게시글 애그리거트
│   ├── shared/            # 공유 도메인 객체
│   └── AbstractEntity.java
├── application/           # 애플리케이션 계층 (유스케이스)
│   ├── member/
│   │   ├── provided/      # Primary Port (인바운드)
│   │   └── required/      # Secondary Port (아웃바운드)
│   └── post/
│       ├── provided/
│       └── required/
└── adapter/               # 어댑터 계층 (기술 구현)
    ├── webapi/            # REST API (Primary Adapter)
    ├── persistence/       # 데이터베이스 (Secondary Adapter)
    ├── security/          # 보안 (Secondary Adapter)
    └── integration/       # 외부 연동 (Secondary Adapter)
```

### 헥사고날 아키텍처 계층

#### 도메인 계층 (`domain`)
- **순수한 비즈니스 로직**과 도메인 규칙
- **외부 의존성 없음** (프레임워크 독립적)
- **애그리거트**, **엔티티**, **값 객체**로 구성

#### 애플리케이션 계층 (`application`)  
- **유스케이스 구현** 및 도메인 객체들의 협력 조율
- **포트 인터페이스** 정의 (Provided/Required Interface)
- **트랜잭션 경계** 관리

#### 어댑터 계층 (`adapter`)
- **외부 시스템과의 연동** 및 기술적 세부 구현
- **Primary Adapter**: 외부에서 들어오는 요청 처리 (REST API)
- **Secondary Adapter**: 외부로 나가는 요청 처리 (DB, 외부 API)

## 🌟 주요 기능

### 현재 구현된 기능
- **회원 관리**: 회원가입, 로그인, 프로필 관리
- **인증/보안**: JWT 기반 토큰 인증
- **게시글 관리**: CRUD 기능 및 상태 관리
- **아키텍처 테스트**: ArchUnit으로 계층 분리 검증

### 구현 예정 기능
- **댓글 시스템**: 댓글 및 대댓글
- **파일 업로드**: 이미지 및 첨부파일
- **검색 기능**: 게시글 및 회원 검색
- **알림 시스템**: 실시간 알림

## 🛠️ 개발 가이드

### Gradle 주요 명령어
```bash
./gradlew clean          # 빌드 정리
./gradlew build          # 프로젝트 빌드
./gradlew test           # 테스트 실행
./gradlew bootRun        # 애플리케이션 실행
```

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 아키텍처 테스트만 실행
./gradlew test --tests "*ArchitectureTest*"

# 도메인 테스트만 실행
./gradlew test --tests "*domain*"
```

## 📖 문서

- **[도메인 모델](DOMAIN_MODEL.md)**: 비즈니스 도메인 상세 분석
- **[개발 가이드](DEVELOPMENT_GUIDE.md)**: 개발 원칙과 실습 방법
- **[용어 사전](GLOSSARY.md)**: 프로젝트 전용 용어 정의
- **[API 문서](API_REFERENCE.md)**: REST API 상세 스펙

## 🏆 개발 원칙

### TDD (Test-Driven Development)
1. **Red**: 실패하는 테스트 작성
2. **Green**: 테스트를 통과하는 최소한의 코드 구현
3. **Refactor**: 코드 개선 및 중복 제거

### 도메인 모델 패턴
- **풍부한 도메인 모델**: 비즈니스 로직이 도메인 객체에 위치
- **애그리거트**: 데이터 변경의 일관성 경계
- **값 객체**: 원시 타입 대신 의미 있는 객체 사용

### 코드 품질
- **SOLID 원칙** 준수
- **높은 응집도, 낮은 결합도**
- **명시적 의존성** (Constructor Injection)
- **불변성** 우선 고려

## 🚦 개발 상태

| 기능 | 상태 | 비고 |
|------|------|------|
| 헥사고날 아키텍처 | ✅ 완료 | 계층 분리 완성 |
| 회원 도메인 | ✅ 완료 | 가입, 로그인, 프로필 |
| 게시글 도메인 | ✅ 완료 | CRUD 및 상태 관리 |
| JWT 인증 | ✅ 완료 | 토큰 기반 인증 |
| 데이터베이스 연동 | 🚧 진행중 | JPA 매핑 구현 중 |
| REST API | 🚧 진행중 | 기본 엔드포인트 구현 |
| 댓글 시스템 | ⏳ 예정 | 설계 단계 |
| 파일 업로드 | ⏳ 예정 | 설계 단계 |

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/amazing-feature`)
3. Follow TDD principles and architectural guidelines
4. Commit your Changes (`git commit -m 'Add amazing feature'`)
5. Push to the Branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

### 기여 가이드라인
- **TDD 필수**: 모든 기능은 테스트 먼저 작성
- **아키텍처 준수**: 헥사고날 아키텍처 원칙 준수
- **도메인 우선**: 비즈니스 로직은 도메인에 위치
- **코드 리뷰**: PR 전 코드 리뷰 필수

---

**See** 프로젝트는 현대적인 Spring 애플리케이션 개발의 모범 사례를 보여주는 레퍼런스 프로젝트입니다. 헥사고날 아키텍처와 도메인 주도 설계를 통해 변경에 유연하고 테스트 가능한 시스템을 구축하는 방법을 학습할 수 있습니다.
