# See Project

See는 사용자들이 다양한 콘텐츠를 작성하고 공유하며 활발하게 소통하는 온라인 커뮤니티 플랫폼입니다.

## 🚀 빠른 시작

### 필수 요구사항
- Java 21
- Docker & Docker Compose

### 로컬 개발 환경 실행

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd see/back
   ```

2. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```
   
   Spring Boot Docker Compose 지원으로 MySQL 컨테이너가 자동으로 실행됩니다.

3. **애플리케이션 접속**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

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

- **호스트**: localhost:3306
- **데이터베이스**: see_dev
- **사용자**: see_user
- **비밀번호**: see_password

## 🛠️ 개발 도구

### Gradle 주요 명령어
```bash
./gradlew clean          # 빌드 정리
./gradlew build          # 프로젝트 빌드
./gradlew test           # 테스트 실행
./gradlew bootRun        # 애플리케이션 실행
```

### API 문서
- Swagger UI: http://localhost:8080/swagger-ui.html

## 📚 기술 스택

- **Backend**: Spring Boot 3.4.5, Java 21
- **Database**: MySQL 8.0
- **Build Tool**: Gradle (Kotlin DSL)
- **Security**: Spring Security, JWT
- **Documentation**: SpringDoc OpenAPI 3
- **Container**: Docker Compose

## 🏗️ 프로젝트 구조

```
src/
├── main/
│   ├── java/dooya/see/
│   │   ├── admin/          # 관리자 기능
│   │   ├── auth/           # 인증/인가
│   │   ├── common/         # 공통 기능
│   │   ├── post/           # 게시글 관리
│   │   └── member/           # 사용자 관리
│   └── resources/
│       ├── application.yml
│       └── application-prod.yml
└── test/                   # 테스트 코드
```

## 🌟 주요 기능

- **사용자 관리**: 회원가입, 로그인, 프로필 관리
- **게시글**: 콘텐츠 작성, 조회, 수정, 삭제
- **인증/보안**: JWT 기반 인증, Spring Security
- **파일 업로드**: AWS S3 연동

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다.
