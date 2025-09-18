package dooya.see.adapter.webapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.adapter.webapi.dto.MemberAuthResponse;
import dooya.see.adapter.webapi.dto.PostCreateResponse;
import dooya.see.adapter.webapi.dto.PostDetailResponse;
import dooya.see.application.member.provided.MemberRegister;
import dooya.see.domain.member.MemberAuthRequest;
import dooya.see.domain.member.MemberFixture;
import dooya.see.domain.member.MemberRegisterRequest;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostCreateRequest;
import dooya.see.domain.post.PostStatus;
import dooya.see.domain.post.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;

import static dooya.see.domain.member.MemberFixture.createMemberAuthRequest;
import static dooya.see.domain.member.MemberFixture.createMemberRegisterRequest;
import static dooya.see.domain.post.PostFixture.createPostRequest;
import static dooya.see.domain.post.PostFixture.updateAllFieldsRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RequiredArgsConstructor
class PostApiTest {
    final ObjectMapper objectMapper;
    final MockMvcTester mvcTester;
    final MemberRegister memberRegister;
    
    @Nested
    class 게시글_생성 {
        @Test
        void 로그인한_사용자가_게시글을_생성할_수_있다() throws JsonProcessingException, UnsupportedEncodingException {
            String token = createMemberAndGetToken();

            PostCreateRequest request = createPostRequest(true);
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/posts")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            assertThat(result).hasStatusOk();

            PostCreateResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostCreateResponse.class);

            assertThat(response.postId()).isNotNull();
            assertThat(response.title()).isEqualTo(request.title());
            assertThat(response.category()).isEqualTo(request.category());
            assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        void 토큰_없이_게시글_생성_요청_시_401_Unauthorized가_발생한다() throws JsonProcessingException {
            PostCreateRequest request = createPostRequest(true);
            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.post().uri("/api/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    class 게시글_조회 {
        @Test
        void 게시글_조회_시_조회수가_1_증가한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.postId()).isEqualTo(postId);
        }


        @Test
        void 비로그인_사용자가_공개되지_않은_게시글_조회_시_403_Forbidden이_발생한다 () throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long draftPostId = createAndDraftPost(token);

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", draftPostId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 로그인한_사용자가_다른_사용자의_비공개_게시글_조회_시_403_Forbidden이_발생한다 () throws UnsupportedEncodingException, JsonProcessingException {
            String authorToken = createMemberAndGetToken();
            Long draftPostId = createAndDraftPost(authorToken);

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", draftPostId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
            }

        @Test
        void 작성자는_본인의_비공개_게시글을_조회할_수_있다 () throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long draftPostId = createAndDraftPost(token);

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", draftPostId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.postId()).isEqualTo(draftPostId);
            assertThat(response.status()).isEqualTo(PostStatus.DRAFT);
        }

        @Test
        void 비로그인_사용자가_숨김_처리된_게시글_조회_시_403_Forbidden이_발생한다 () throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            // 게시글을 숨김 처리
            mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                        .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 작성자는_본인의_숨김_처리된_게시글을_조회할_수_있다 () throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            // 게시글을 숨김 처리
            mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
        }

        @Test
        void 다른_사용자가_숨김_처리된_게시글_조회_시_403_Forbidden이_발생한다 () throws UnsupportedEncodingException, JsonProcessingException {
            String authorToken = createMemberAndGetToken();
            Long postId = createAndPublishPost(authorToken);

            // 게시글을 숨김 처리
            mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + authorToken)
                    .exchange();

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 로그인한_다른_사용자가_게시글을_조회할_수_있다 () throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.get().uri("/api/posts/{id}", postId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.postId()).isEqualTo(postId);
        }

    }

    @Nested
    class 게시글_수정 {
        @Test
        void 작성자가_게시글을_수정할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            PostUpdateRequest request = updateAllFieldsRequest();

            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/posts/{id}", postId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.body()).isEqualTo("수정된 내용");
        }

        @Test
        void 토큰_없이_게시글_수정_요청_시_401_Unauthorized가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            PostUpdateRequest request = updateAllFieldsRequest();

            String requestJson = objectMapper.writeValueAsString(request);

            MvcTestResult result = mvcTester.put().uri("/api/posts/{id}", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_수정_시_403_Forbidden이_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            PostUpdateRequest request = updateAllFieldsRequest();

            String requestJson = objectMapper.writeValueAsString(request);

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.put().uri("/api/posts/{id}", postId)
                    .header("Authorization", "Bearer " + readerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson).exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    class 게시글_발행 {
        @Test
        void 작성자가_임시저장된_게시글을_발행할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndDraftPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(response.publishedAt()).isNotNull();
        }

        @Test
        void 토큰_없이_게시글_발행_요청_시_401_Unauthorized가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndDraftPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_발행_시_403_Forbidden이_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndDraftPost(token);

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_발행된_게시글을_다시_발행하려고_하면_409_Conflict가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }
    }

    @Nested
    class 게시글_숨김 {
        @Test
        void 숨김_상태의_게시글을_다시_발행할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/publish", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.status()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(response.publishedAt()).isNotNull();
        }

        @Test
        void 작성자가_발행된_게시글을_숨김_처리할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
        }


        @Test
        void 토큰_없이_게시글_숨김_요청_시_401_Unauthorized가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_숨김_시_403_Forbidden이_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_숨김_처리된_게시글을_다시_숨기려고_하면_409_Conflict가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }

        @Test
        void 작성자가_아닌_사용자가_임시저장_게시글_숨김_시_403_Forbidden이_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndDraftPost(token);

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 작성자가_임시저장_게시글을_숨김_처리할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndDraftPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.status()).isEqualTo(PostStatus.HIDDEN);
        }
    }

    @Nested
    class 게시글_삭제 {
        @Test
        void 작성자가_게시글을_삭제할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.status()).isEqualTo(PostStatus.DELETED);
        }


        @Test
        void 토큰_없이_게시글_삭제_요청_시_401_Unauthorized가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void 작성자가_아닌_사용자가_게시글_삭제_시_403_Forbidden이_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.FORBIDDEN);
        }

        @Test
        void 이미_삭제된_게시글을_다시_삭제하려고_하면_409_Conflict가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            mvcTester.post().uri("/api/posts/{id}/delete", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.CONFLICT);
        }

        @Test
        void 존재하지_않는_게시글을_삭제하려고_하면_404_Not_Found가_발생한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", 999L)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result)
                    .apply(print())
                    .hasStatus(HttpStatus.NOT_FOUND);
        }

        @Test
        void 임시저장_상태의_게시글을_삭제할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndDraftPost(token);

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.status()).isEqualTo(PostStatus.DELETED);
        }

        @Test
        void 숨김_상태의_게시글을_삭제할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            mvcTester.post().uri("/api/posts/{id}/hide", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            MvcTestResult result = mvcTester.post().uri("/api/posts/{id}/delete", postId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            PostDetailResponse response =
                    objectMapper.readValue(result.getResponse().getContentAsString(), PostDetailResponse.class);

            assertThat(response.status()).isEqualTo(PostStatus.DELETED);
        }
    }

    @Nested
    class 게시글_목록_조회 {
        @Test
        void 공개된_게시글_목록을_조회할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token);
            createAndPublishPost(token);
            createAndDraftPost(token);

            MvcTestResult result = mvcTester.get().uri("/api/posts")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(2);
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(posts[1].status()).isEqualTo(PostStatus.PUBLISHED);
        }

        @Test
        void 특정_카테고리의_공개_게시글_목록을_조회할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token); // TECH 카테고리

            MvcTestResult result = mvcTester.get().uri("/api/posts/category/TECH")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].category()).isEqualTo(PostCategory.TECH);
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        }

        @Test
        void 본인의_게시글_목록을_조회하면_모든_상태의_게시글이_반환된다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();

            Long publishedPostId = createAndPublishPost(token);
            createAndDraftPost(token);

            MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", publishedPostId)
                    .exchange();
            PostDetailResponse postDetail = objectMapper.readValue(
                    postResult.getResponse().getContentAsString(), PostDetailResponse.class);
            Long actualMemberId = postDetail.authorId();

            MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/posts", actualMemberId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(2);
        }

        @Test
        void 다른_사용자의_게시글_목록을_조회하면_공개된_게시글만_반환된다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long publishedPostId = createAndPublishPost(token);
            createAndDraftPost(token);

            MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", publishedPostId)
                    .exchange();
            PostDetailResponse postDetail = objectMapper.readValue(
                    postResult.getResponse().getContentAsString(), PostDetailResponse.class);
            Long authorMemberId = postDetail.authorId();

            String readerToken = createSecondMemberAndGetToken();

            MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/posts", authorMemberId)
                    .header("Authorization", "Bearer " + readerToken)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1); // PUBLISHED 게시글만 조회
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        }

        @Test
        void 비로그인_상태로_다른_사용자_게시글을_조회하면_공개된_게시글만_반환된다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long publishedPostId = createAndPublishPost(token);
            createAndDraftPost(token);

            MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", publishedPostId)
                    .exchange();
            PostDetailResponse postDetail = objectMapper.readValue(
                    postResult.getResponse().getContentAsString(), PostDetailResponse.class);
            Long authorMemberId = postDetail.authorId();

            MvcTestResult result = mvcTester.get().uri("/api/members/{memberId}/posts", authorMemberId)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        }
    }

    @Nested
    class 게시글_검색 {
        @Test
        void 비로그인_사용자가_카테고리로_게시글을_검색하면_공개된_게시글만_반환된다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token);  // PUBLISHED 상태
            createAndDraftPost(token);    // DRAFT 상태

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?category=TECH")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1); // 공개된 게시글만 1개 반환
            assertThat(posts).allMatch(post -> post.category() == PostCategory.TECH);
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        }

        @Test
        void 로그인한_사용자가_본인의_카테고리별_게시글을_검색하면_모든_상태가_반환된다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();

            Long publishedPostId = createAndPublishPost(token);
            createAndDraftPost(token);

            // 회원 ID 얻기
            MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", publishedPostId).exchange();
            PostDetailResponse postDetail = objectMapper.readValue(
                    postResult.getResponse().getContentAsString(), PostDetailResponse.class);
            Long memberId = postDetail.authorId();

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?category=TECH&memberId=" + memberId)
                    .header("Authorization", "Bearer " + token)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(2); // 본인 게시글이므로 PUBLISHED + DRAFT 모두 반환
            assertThat(posts).allMatch(post -> post.category() == PostCategory.TECH);
            assertThat(posts).allMatch(post -> post.authorId().equals(memberId));
        }

        @Test
        void 키워드로_게시글을_검색할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token); // "테스트 게시글 제목입니다" 포함

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?keyword=테스트")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].title()).contains("테스트");
        }

        @Test
        void 제목_키워드로_게시글을_검색할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token);

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?titleKeyword=게시글")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].title()).contains("게시글");
        }

        @Test
        void 내용_키워드로_게시글을_검색할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token); // "테스트 게시글 내용입니다" 포함

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?contentKeyword=내용")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].body()).contains("내용");
        }

        @Test
        void 특정_회원이_작성한_게시글을_검색할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            Long postId = createAndPublishPost(token);

            // 회원 ID 얻기
            MvcTestResult postResult = mvcTester.get().uri("/api/posts/{id}", postId).exchange();
            PostDetailResponse postDetail = objectMapper.readValue(
                    postResult.getResponse().getContentAsString(), PostDetailResponse.class);
            Long memberId = postDetail.authorId();

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?memberId=" + memberId)
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].authorId()).isEqualTo(memberId);
        }

        @Test
        void 게시글_상태로_검색할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token);
            createAndDraftPost(token);

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?status=PUBLISHED")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        }

        @Test
        void 여러_조건을_조합하여_게시글을_검색할_수_있다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token);
            createAndDraftPost(token);

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?category=TECH&status=PUBLISHED&keyword=테스트")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1);
            assertThat(posts[0].category()).isEqualTo(PostCategory.TECH);
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
            assertThat(posts[0].title()).contains("테스트");
        }

        @Test
        void 검색_조건에_맞는_게시글이_없으면_빈_배열을_반환한다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token);

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search?keyword=존재하지않는키워드")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).isEmpty();
        }

        @Test
        void 비로그인_사용자가_검색_조건_없이_요청하면_공개된_게시글만_반환된다() throws UnsupportedEncodingException, JsonProcessingException {
            String token = createMemberAndGetToken();
            createAndPublishPost(token);
            createAndDraftPost(token);

            MvcTestResult result = mvcTester.get()
                    .uri("/api/posts/search")
                    .exchange();

            assertThat(result).hasStatusOk();

            String responseContent = result.getResponse().getContentAsString();
            PostDetailResponse[] posts = objectMapper.readValue(responseContent, PostDetailResponse[].class);

            assertThat(posts).hasSize(1); // 공개된 게시글만 반환
            assertThat(posts[0].status()).isEqualTo(PostStatus.PUBLISHED);
        }
    }

    // Helper Methods
    private String createMemberAndGetToken() throws JsonProcessingException, UnsupportedEncodingException {
        memberRegister.register(createMemberRegisterRequest());

        MemberAuthRequest request = createMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(), MemberAuthResponse.class);

        return authResponse.accessToken();
    }

    private Long createAndPublishPost(String token) throws JsonProcessingException, UnsupportedEncodingException {
        PostCreateRequest createRequest = createPostRequest(true);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcTestResult createResult = mvcTester.post().uri("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        PostCreateResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                PostCreateResponse.class
        );

        return createResponse.postId();
    }

    private Long createAndDraftPost(String token) throws JsonProcessingException, UnsupportedEncodingException {
        PostCreateRequest createRequest = createPostRequest(false);
        String requestJson = objectMapper.writeValueAsString(createRequest);

        MvcTestResult createResult = mvcTester.post().uri("/api/posts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        PostCreateResponse createResponse = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                PostCreateResponse.class
        );

        return createResponse.postId();
    }

    private String createSecondMemberAndGetToken() throws JsonProcessingException, UnsupportedEncodingException {
        MemberRegisterRequest secondMemberRequest = MemberFixture.createSecondMemberRegisterRequest();
        memberRegister.register(secondMemberRequest);

        MemberAuthRequest authRequest = MemberFixture.createSecondMemberAuthRequest();
        String requestJson = objectMapper.writeValueAsString(authRequest);

        MvcTestResult loginResult = mvcTester.post().uri("/api/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        MemberAuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                MemberAuthResponse.class
        );

        return authResponse.accessToken();
    }
}