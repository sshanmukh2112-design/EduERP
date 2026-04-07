package com.example.eduerp.controller;

import com.example.eduerp.entity.User;
import com.example.eduerp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userRepository.findById(id).map(existing -> {
            user.setId(id);
            if (user.getPassword() == null)
                user.setPassword(existing.getPassword());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        return userRepository.findById(id).map(user -> {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body("Incorrect current password!");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/students")
    public List<User> getStudents() {
        return userRepository.findAll().stream()
                .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<User> getStudent(@PathVariable Long id) {
        return userRepository.findById(id)
                .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teachers")
    public List<User> getTeachers() {
        return userRepository.findAll().stream()
                .filter(u -> "TEACHER".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
    }

    @GetMapping("/teachers/{id}")
    public ResponseEntity<User> getTeacher(@PathVariable Long id) {
        return userRepository.findById(id)
                .filter(u -> "TEACHER".equalsIgnoreCase(u.getRole()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
