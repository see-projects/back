package dooya.see.admin.presentation;

import dooya.see.auth.config.SecurityConfig;
import dooya.see.auth.util.JwtUtil;
import dooya.see.member.application.service.MemberQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MemberQueryService memberQueryService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @DisplayName("GET 요청 시 userQueryService.getUsers() 호출 여부 검증")
    @WithMockUser(roles = "ADMIN")
    @Test
    void get_WhenCalled_InvokesGetUsers() throws Exception {
        // when
        mockMvc.perform(get("/api/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then
        then(memberQueryService).should().getUsers();
    }
}
