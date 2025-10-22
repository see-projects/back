# 캐시 도입 1단계 – 대상 선정 및 TTL/일관성 정책

## 1. 캐시 도입 목표
- **DB/ES 부하 완화**: 인기 게시글·상세 조회가 몰릴 때 RDB와 Elasticsearch의 TPS 급증을 방지합니다.
- **핵심 사용자 경험 유지**: 메인 피드·게시글 상세 응답 시간을 100 ms 이하로 유지하는 것이 목표입니다.
- **확장 가능성 확보**: 이후 Kafka 기반 증분 갱신, CQRS 분리까지 자연스럽게 확장할 수 있는 기반을 마련합니다.

## 2. 캐시 후보 데이터
| 구분 | 엔드포인트/질의 | 데이터 형태 | 요청 패턴 | 최신성 요구 | 비고 |
| --- | --- | --- | --- | --- | --- |
| 게시글 상세 | `GET /api/posts/{id}` | `PostDetailResponse` | 읽기 많고 수정 적음 | 초 단위 지연 허용 | 가장 먼저 적용 |
| 게시글 목록(공개) | `GET /api/posts` 등 | 목록 + paging | 반복 호출 | 수 초 단위 재사용 | PostStats와 함께 캐시 |
| 게시글 검색 결과 | `GET /api/posts/search` | ID 리스트 & 요약 | 고빈도 검색 | ES 결과 재사용 가능 | ES hit 후 Redis에 결과 보관 |
| 인기 게시글 | 추후 별도 API | 랭킹/집계 | 대시보드/홈 | 주기적 갱신 | Sorted Set 활용 가능 |
| PostStats | 내부 조회 | 조회수/좋아요/댓글 | 이벤트 기반 | 최종 일관성 허용 | 통계 API 준비 시 캐시 활용 |
| 사용자 프로필 | `GET /api/members/me` | member snapshot | 빈도 낮음 | 강한 일관성 요구 | 캐시 대상 제외 (오버엔지니어링 방지) |

## 3. TTL 및 일관성 정책
- **게시글 상세**: 기본 TTL 60초, 이벤트 무효화(`PostUpdated`,`PostDeleted`,`PostPublished`) 발생 시 즉시 삭제.
- **공개 목록/검색 결과**: TTL 30초~60초, 페이지/필터별 키 구성. 새 글 발행 혹은 상태 변경 이벤트 시 관련 키 무효화.
- **인기 게시글/랭킹**: TTL 5분, 별도 Pre-warm 배치 또는 Sorted Set 증분 업데이트.
- **PostStats**: 이벤트 기반 증분 적용 시 TTL 대신 캐시된 값을 직접 갱신(Write-through) 또는 30초 TTL + 이벤트 무효화 중 선택.
- **캐시 미스 처리**: 항상 원본(RDB/ES)을 조회하여 저장, 실패 시 캐시 저장 생략(에러 캐시 금지).

## 4. 키 설계 초안
```
post:detail:{postId}
post:list:public:{page}:{size}
post:list:category:{category}:{page}:{size}
post:search:{hash(keyword,title,content,...)}:{page}:{size}
post:stats:{postId}
```
- 키는 소문자 + 콜론 구분으로 통일, page/size 포함.
- 검색 조건은 파라미터 직렬화 후 해시(MD5 등)로 압축.
- 추후 멀티 테넌시 고려 시 prefix 추가 가능.

## 5. 무효화 전략 설계
1. **도메인 이벤트 -> 캐시 무효화**  
   - Post 이벤트: `PostCreated`, `PostUpdated`, `PostDeleted`, `PostPublished`, `PostHidden`.  
   - Comment 이벤트: `CommentCreated`, `CommentDeleted`, `CommentHidden`.  
   - 이벤트 수신 시 연관 키 계산 후 삭제.
2. **TTL 보조**  
   - 이벤트 누락 또는 장애에 대비해 적절한 TTL을 유지.
3. **Fallback**  
   - 캐시 갱신 실패 시 로그 경고만 남기고 요청 흐름은 진행(서킷 브레이커 역할).

## 6. 모니터링 지표
- `cache_hit_total`, `cache_miss_total`, `cache_eviction_total`
- 이벤트 처리 지연(`event_process_latency`) 및 실패 횟수
- Redis 연결 풀 지표(Max connection, Timeout)
- 95/99 percentile 응답 시간 비교(before/after) – API 관측 필요

## 7. 오픈 이슈 & 의사결정 포인트
- PostStats를 **무효화 vs 직접 쓰기** 중 어떤 방식으로 유지할지 확정.
- 검색 결과 캐시에서 ES 최신성(near-real-time)과의 차이를 어느 정도 허용할지 SLA 정의.
- Redis 운영 방식(Standalone → Sentinel/Cluster) 및 용량 계획 수립.
- 향후 Kafka 메시지를 이용한 비동기 갱신을 1차 릴리스에 포함할지, 후속 단계로 분리할지 결정.

## 8. 다음 단계 제안
1. 위 정책을 근거로 Redis 연결 설정과 템플릿 구성
2. 게시글 상세 캐시 구현 & 이벤트 무효화 PoC
3. Kafka 연동 확장 및 모니터링 지표 추가
4. 인기글/검색 캐시 도입 및 TTL 튜닝

---
해당 문서는 1단계 분석 결과이며, 구현 단계에서 추가 학습/Validation 결과에 따라 TTL·대상 범위를 조정할 수 있습니다.
