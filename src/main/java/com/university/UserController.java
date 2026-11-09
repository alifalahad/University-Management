package com.university;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CRUD for /api/admins, /api/teachers and /api/students.
// The {type} in the URL decides which role we work with.
@RestController
@RequestMapping("/api/{type:admins|teachers|students}")
public class UserController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserController(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // "admins" -> "ADMIN", "teachers" -> "TEACHER", "students" -> "STUDENT"
    private String roleOf(String type) {
        return type.substring(0, type.length() - 1).toUpperCase();
    }

    private User findUser(String type, Long id) {
        return repo.findById(id)
                .filter(u -> u.getRole().equals(roleOf(type)))
                .orElse(null);
    }

    // GET /api/students -> list all
    @GetMapping
    public List<User> getAll(@PathVariable String type) {
        return repo.findByRole(roleOf(type));
    }

    // GET /api/students/1 -> get one
    @GetMapping("/{id}")
    public ResponseEntity<User> getOne(@PathVariable String type, @PathVariable Long id) {
        User user = findUser(type, id);
        if (user == null) return ResponseEntity.notFound().build();          // 404
        return ResponseEntity.ok(user);                                       // 200
    }

    // POST /api/students -> create
    @PostMapping
    public ResponseEntity<User> create(@PathVariable String type, @RequestBody User user) {
        if (repo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();        // 409
        }
        user.setId(null);
        user.setRole(roleOf(type));
        user.setPassword(encoder.encode(user.getPassword()));                 // hash password
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(user)); // 201
    }

    // PUT /api/students/1 -> update
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String type, @PathVariable Long id, @RequestBody User data) {
        User user = findUser(type, id);
        if (user == null) return ResponseEntity.notFound().build();          // 404
        user.setName(data.getName());
        user.setUsername(data.getUsername());
        if (data.getPassword() != null) {
            user.setPassword(encoder.encode(data.getPassword()));
        }
        return ResponseEntity.ok(repo.save(user));                            // 200
    }

    // DELETE /api/students/1 -> delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String type, @PathVariable Long id) {
        User user = findUser(type, id);
        if (user == null) return ResponseEntity.notFound().build();          // 404
        repo.delete(user);
        return ResponseEntity.noContent().build();                            // 204
    }
}
