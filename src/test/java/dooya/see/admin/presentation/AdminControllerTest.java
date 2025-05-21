package dooya.see.admin.presentation;

import dooya.see.auth.util.JwtUtil;
import dooya.see.common.AdminFixture;
import dooya.see.common.UserFixture;
import dooya.see.user.domain.Role;
import dooya.see.user.domain.User;
import dooya.see.user.infrastructure.UserJpaRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("classpath:init.sql")
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @DisplayName("유저 목록 조회 성공 테스트")
    @Test
    void admin_userSelect_success() throws Exception {
        // Arrange
        User testAdmin = userJpaRepository.save(AdminFixture.testAdmin());
        String testToken = jwtUtil.createAccessToken(testAdmin.getId(), testAdmin.getEmail(), Role.valueOf(testAdmin.getRole().getRoleName()));

        // Act & Assert
        mockMvc.perform(get("/api/admin")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("유저 목록 조회 실패 테스트")
    @Test
    void admin_userSelect_fail() throws Exception {
        User testAdmin = userJpaRepository.save(UserFixture.testUser());
        String testToken = jwtUtil.createAccessToken(testAdmin.getId(), testAdmin.getEmail(), Role.valueOf(testAdmin.getRole().getRoleName()));

        // Act & Assert
        mockMvc.perform(get("/api/admin")
                        .cookie(new Cookie("Authorization", testToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("접근 권한이 없는 사용자입니다."));
    }
}
