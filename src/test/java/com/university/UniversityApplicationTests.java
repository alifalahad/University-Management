package com.university;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// INTEGRATION TEST — starts the full application with H2 database
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UniversityApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository repo;

    @Test
    void contextLoads() {
        // Verifies the entire Spring context starts without errors
    }

    @Test
    void fullCrudFlow() {
        // 1. Create an admin user directly in DB for authentication
        User admin = new User();
        admin.setName("TestAdmin");
        admin.setUsername("testadmin");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
        admin.setRole("ADMIN");
        repo.save(admin);

        // 2. Create a student via the API (authenticated as admin)
        //    Use a Map instead of User, because User.password has @JsonProperty(WRITE_ONLY)
        //    which prevents it from being serialized to JSON
        Map<String, String> studentData = Map.of(
                "name", "Jane",
                "username", "jane123",
                "password", "pass123"
        );

        ResponseEntity<User> createResponse = restTemplate
                .withBasicAuth("testadmin", "admin123")
                .postForEntity("/api/students", studentData, User.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().getName()).isEqualTo("Jane");

        // 3. GET the student back
        Long studentId = createResponse.getBody().getId();
        ResponseEntity<User> getResponse = restTemplate
                .withBasicAuth("testadmin", "admin123")
                .getForEntity("/api/students/" + studentId, User.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getUsername()).isEqualTo("jane123");

        // 4. DELETE the student
        restTemplate.withBasicAuth("testadmin", "admin123")
                .delete("/api/students/" + studentId);

        // 5. Verify it's gone
        ResponseEntity<User> afterDelete = restTemplate
                .withBasicAuth("testadmin", "admin123")
                .getForEntity("/api/students/" + studentId, User.class);

        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
