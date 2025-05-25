package dooya.see.post.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import dooya.see.auth.config.SecurityConfig;
import dooya.see.auth.domain.LoginUser;
import dooya.see.auth.util.JwtUtil;
import dooya.see.common.PostFixture;
import dooya.see.post.application.service.PostQueryService;
import dooya.see.post.application.dto.PostCommand;
import dooya.see.post.application.service.PostCreateService;
import dooya.see.post.presentation.dto.PostRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private PostQueryService postQueryService;

    @MockitoBean
    private PostCreateService postCreateService;

    @DisplayName("POST 요청 시 postCreateService.createPost() 호출 여부 검증")
    @Test
    void post_WhenCalled_InvokesCreatePost() throws Exception {
        // given
        PostRequest request = PostFixture.request();
        PostCommand command = PostFixture.command();
        LoginUser loginUser = new LoginUser(1L, "email", "USER");

        given(postCreateService.createPost(anyString(), any(PostCommand.class))).willReturn(PostFixture.result());

        // when
        mockMvc.perform(post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(loginUser))
                        .with(csrf()))
                .andExpect(status().isCreated());

        // then
        then(postCreateService).should().createPost("email", command);
    }

    @DisplayName("GET 요청 시 postQueryService.getPosts() 호출 여부 검증")
    @Test
    void post_WhenCalled_InvokesGetPost() throws Exception {
        // when
        mockMvc.perform(get("/api/post"))
                .andExpect(status().isOk());

        // then
        then(postQueryService).should().getPosts();
    }
}
