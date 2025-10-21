# RedisTemplate 설정 & 키 스키마 정의 절차

캐시 1단계 구현(“RedisTemplate 설정 + 키 스키마 정의”)을 진행할 때 따라야 할 단계별 가이드입니다.

## 1. 환경 설정 값 정의
1. Redis 접속 정보를 `application.yml` 또는 프로파일별 설정에 추가합니다.  
   - 예: `spring.data.redis.host`, `spring.data.redis.port`, `spring.data.redis.password`.  
2. 로컬·운영 환경에서 값을 어떻게 주입할지(환경 변수, docker-compose 등) 사전에 정리합니다.

## 2. Redis 설정 클래스 작성
1. 패키지 예시: `dooya.see.adapter.integration.cache`.  
2. `@Configuration` 클래스를 만들고 다음 빈을 등록합니다.
   - `RedisConnectionFactory` (Lettuce 클라이언트 사용 권장)  
   - `RedisTemplate<String, ?>` (직렬화 전략 선택: `StringRedisSerializer`, `GenericJackson2JsonRedisSerializer` 등)  
3. 필요 시 `ObjectMapper` 등 공용 Bean을 주입하거나 별도 설정을 만듭니다.

## 3. 키 스키마 유틸리티 구현
1. 패키지 예시: `dooya.see.adapter.integration.cache.key`.  
2. `PostCacheKey`와 같은 유틸 클래스를 만들고 정적 메서드로 키를 생성합니다.
   - `detail(postId)`, `publicList(page,size)`, `search(hash,page,size)` 등.  
3. 검색 조건 해시가 필요하면 `MessageDigest` 혹은 외부 라이브러리를 사용합니다.  
4. 키 규칙: 소문자 + 콜론(`:`) 구분, null/빈 값은 normalize 후 처리.

## 4. TTL/설정 상수 관리
1. `@ConfigurationProperties("see.cache")` 같은 설정 클래스를 만들어 TTL을 외부화합니다.  
2. 게시글 상세/목록/검색/통계별 기본 TTL 값을 명시하고 `application.yml`에 설정합니다.  
3. 추후 모니터링 결과에 따라 조정할 수 있도록 기본값과 설명을 문서화합니다.

## 5. 테스트 및 검증 메모
1. 키 생성 유틸의 단위 테스트를 작성해 입력→키 문자열을 검증합니다.  
2. RedisTemplate이 Bean으로 정상 등록되는지 `@SpringBootTest` 혹은 슬라이스 테스트로 확인합니다.  
3. 향후 Kafka 이벤트와 연동할 때 사용할 `CacheService` 인터페이스/구현체 초안을 생각해둡니다.

위 순서를 순차적으로 따라가면 RedisTemplate과 키 스키마를 준비한 뒤, 게시글 상세/목록 캐시에 재사용할 수 있습니다.
