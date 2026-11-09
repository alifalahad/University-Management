package com.university;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// UNIT TEST — only loads the web layer, repository is mocked (fake)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository repo;

    @MockBean
    private PasswordEncoder encoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnAllStudents() throws Exception {
        User student = new User();
        student.setId(1L);
        student.setName("John");
        student.setUsername("john123");
        student.setRole("STUDENT");

        when(repo.findByRole("STUDENT")).thenReturn(List.of(student));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].username").value("john123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenStudentNotFound() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenDuplicateUsername() throws Exception {
        User existing = new User();
        existing.setUsername("john123");
        when(repo.findByUsername("john123")).thenReturn(Optional.of(existing));

        User newUser = new User();
        newUser.setUsername("john123");
        newUser.setName("John");
        newUser.setPassword("pass");

        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }
}
