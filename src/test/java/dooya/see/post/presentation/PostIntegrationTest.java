package dooya.see.post.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.util.JwtUtil;
import dooya.see.member.domain.Member;
import dooya.see.post.presentation.dto.PostRequest;
import dooya.see.member.infrastructure.MemberJpaRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static dooya.see.common.PostFixture.*;
import static dooya.see.common.UserFixture.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("classpath:init.sql")
public class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String testToken;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberJpaRepository.save(testUser());
        testToken = jwtUtil.createAccessToken(testMember.getId(), testMember.getEmail(), testMember.getRole());
    }

    @DisplayName("게시글 작성 성공 테스트")
    @Test
    void user_Post_Success() throws Exception {
        // Arrange
        PostRequest request = request();

        // Act && Assert
        mockMvc.perform(post("/api/post")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nickName").value(testMember.getNickName()))
                .andExpect(jsonPath("$.title").value(request.title()))
                .andExpect(jsonPath("$.content").value(request.content()));
    }

    @DisplayName("게시글 작성 실패 테스트 - 개별 필드 유효성 검증")
    @ParameterizedTest(name = "{index} => 필드 = {0}, 메시지 = {1}")
    @MethodSource("invalidFieldProvider")
    void post_Fail_InvalidField(PostRequest request, String field, String message) throws Exception {
        mockMvc.perform(post("/api/post")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.validationErrors[0].field").value(field))
                .andExpect(jsonPath("$.validationErrors[0].message").value(message));
    }

    private static Stream<Arguments> invalidFieldProvider() {
        return Stream.of(
                Arguments.of(
                        request().toBuilder().title("").build(),
                        "title", "제목은 비어 있을 수 없습니다"
                ),
                Arguments.of(
                        request().toBuilder().content("").build(),
                        "content", "내용은 비어 있을 수 없습니다"
                )
        );
    }

    @DisplayName("게시글 조회 성공 테스트")
    @Test
    void post_Get_Success() throws Exception {
        // Arrange
        saveTestPost();

        // Act && Assert
        mockMvc.perform(get("/api/post")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].nickName").exists())
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].content").exists())
                .andExpect(jsonPath("$.content[0].createdDate").exists())
                .andExpect(jsonPath("$.content[0].updatedDate").exists());
    }

    private long saveTestPost() throws Exception {
        PostRequest request = request();

        MvcResult result = mockMvc.perform(post("/api/post")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.get("id").asLong();
    }

    @DisplayName("단일 게시글 조회 성공 테스트")
    @Test
    void postId_GetPost_Success() throws Exception {
        // Arrange
        Long id = saveTestPost();

        // Act && Assert
        mockMvc.perform(get("/api/post/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nickName").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.createdDate").exists())
                .andExpect(jsonPath("$.updatedDate").exists());
    }

    @DisplayName("단일 게시글 조회 실패 테스트 - 게시글이 존재하지 않을 경우")
    @Test
    void postId_GetPost_Fail() throws Exception {
        // Arrange
        long id = saveTestPost();
        id = 999L;

        // Act && Assert
        mockMvc.perform(get("/api/post/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("게시글이 존재하지 않습니다."));
    }
}