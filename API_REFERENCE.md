# See API 문서

See 커뮤니티 플랫폼의 REST API 문서입니다.

## 📌 기본 정보

- **Base URL**: `http://localhost:8080`
- **API Version**: v1
- **Content-Type**: `application/json`
- **Authentication**: JWT Bearer Token

## 🔐 인증

### JWT 토큰 사용
모든 인증이 필요한 API는 Authorization 헤더에 JWT 토큰을 포함해야 합니다.

```http
Authorization: Bearer <your-jwt-token>
```

## 👥 회원 관리 API

### 회원 가입
새로운 회원을 등록합니다.

```http
POST /api/members
```

#### Request Body
```json
{
  "email": {
    "address": "user@example.com"
  },
  "nickname": "사용자123",
  "password": "password123!"
}
```

#### Request Schema
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| email.address | string | Yes | 이메일 주소 (RFC 5321 표준) |
| nickname | string | Yes | 닉네임 (1-20자, 중복 불가) |
| password | string | Yes | 비밀번호 (영문+숫자+특수문자, 8자 이상) |

#### Response
```json
{
  "memberId": 1,
  "email": "user@example.com",
  "nickname": "사용자123",
  "status": "PENDING",
  "registeredAt": "2024-01-15T10:30:00"
}
```

#### Status Codes
- `200 OK`: 가입 성공
- `400 Bad Request`: 입력 데이터 검증 실패
- `409 Conflict`: 이메일 또는 닉네임 중복

### 로그인
이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.

```http
POST /api/auth/login
```

#### Request Body
```json
{
  "email": {
    "address": "user@example.com"
  },
  "password": "password123!"
}
```

#### Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "member": {
    "memberId": 1,
    "email": "user@example.com",
    "nickname": "사용자123",
    "status": "ACTIVE"
  }
}
```

#### Status Codes
- `200 OK`: 로그인 성공
- `400 Bad Request`: 입력 데이터 오류
- `401 Unauthorized`: 인증 실패

## 📝 게시글 관리 API

### 게시글 작성
새로운 게시글을 작성합니다. (인증 필요)

```http
POST /api/posts
```

#### Headers
```http
Authorization: Bearer <your-jwt-token>
```

#### Request Body
```json
{
  "title": "게시글 제목",
  "body": "게시글 내용입니다. 마크다운 형식도 지원합니다.",
  "category": "TECH",
  "status": "PUBLISHED"
}
```

#### Request Schema
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| title | string | Yes | 게시글 제목 (1-100자) |
| body | string | Yes | 게시글 내용 (1-50,000자) |
| category | string | Yes | 카테고리 (GENERAL, TECH, NEWS, QNA, NOTICE) |
| status | string | No | 상태 (DRAFT, PUBLISHED, 기본값: DRAFT) |

#### Response
```json
{
  "postId": 1,
  "title": "게시글 제목",
  "body": "게시글 내용입니다. 마크다운 형식도 지원합니다.",
  "category": "TECH",
  "status": "PUBLISHED",
  "authorId": 1,
  "authorNickname": "사용자123",
  "createdAt": "2024-01-16T10:00:00",
  "updatedAt": "2024-01-16T10:00:00",
  "publishedAt": "2024-01-16T10:00:00",
  "viewCount": 0
}
```

#### Status Codes
- `201 Created`: 작성 성공
- `400 Bad Request`: 입력 데이터 오류
- `401 Unauthorized`: 인증 실패

### 게시글 목록 조회
게시글 목록을 페이징하여 조회합니다.

```http
GET /api/posts
```

#### Query Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page | integer | 0 | 페이지 번호 (0부터 시작) |
| size | integer | 20 | 페이지 크기 (최대 100) |
| category | string | - | 카테고리 필터 (GENERAL, TECH, NEWS, QNA, NOTICE) |
| sort | string | createdAt,desc | 정렬 기준 (createdAt, viewCount, title) |

#### Example Request
```http
GET /api/posts?page=0&size=10&category=TECH&sort=createdAt,desc
```

#### Response
```json
{
  "content": [
    {
      "postId": 1,
      "title": "게시글 제목",
      "body": "게시글 내용 미리보기...",
      "category": "TECH",
      "status": "PUBLISHED",
      "authorId": 1,
      "authorNickname": "사용자123",
      "createdAt": "2024-01-16T10:00:00",
      "viewCount": 42,
      "commentCount": 5
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "direction": "DESC",
      "property": "createdAt"
    }
  },
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true
}
```

## 🚨 오류 응답

### 공통 오류 형식
모든 오류 응답은 다음 형식을 따릅니다:

```json
{
  "timestamp": "2024-01-16T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "이메일 형식이 올바르지 않습니다.",
  "path": "/api/members"
}
```

### HTTP 상태 코드

#### 2xx 성공
- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공

#### 4xx 클라이언트 오류
- `400 Bad Request`: 잘못된 요청 (유효성 검증 실패)
- `401 Unauthorized`: 인증 실패 (토큰 없음 또는 만료)
- `403 Forbidden`: 권한 없음 (인증됐으나 접근 권한 없음)
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 중복 (이메일, 닉네임 등)

#### 5xx 서버 오류
- `500 Internal Server Error`: 서버 내부 오류

## 📝 사용 예시

### JavaScript (Fetch API)
```javascript
// 회원가입
const registerMember = async () => {
  const response = await fetch('/api/members', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: { address: 'user@example.com' },
      nickname: '사용자123',
      password: 'password123!'
    })
  });
  
  const result = await response.json();
  console.log(result);
};

// 로그인 후 게시글 작성
const createPost = async (token) => {
  const response = await fetch('/api/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      title: '새로운 게시글',
      body: '게시글 내용입니다.',
      category: 'TECH',
      status: 'PUBLISHED'
    })
  });
  
  const post = await response.json();
  console.log(post);
};
```

### curl 예시
```bash
# 회원가입
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "email": {"address": "user@example.com"},
    "nickname": "사용자123",
    "password": "password123!"
  }'

# 게시글 작성 (토큰 필요)
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "title": "새로운 게시글",
    "body": "게시글 내용입니다.",
    "category": "TECH",
    "status": "PUBLISHED"
  }'
```

---

이 API 문서는 See 프로젝트의 **REST API 사용법**을 상세히 설명하며, **프론트엔드 개발자**와 **외부 개발자**가 쉽게 연동할 수 있도록 돕습니다.
