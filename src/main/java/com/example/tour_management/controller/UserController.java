package com.example.tour_management.controller;

import com.example.tour_management.dto.user.UserRequest;
import com.example.tour_management.dto.user.UserResponse;
import com.example.tour_management.service.PasswordResetTokenService;
import com.example.tour_management.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(){
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserRequest req ){
        return ResponseEntity.ok(userService.updateCurrentUser(req));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/search-user")
    public ResponseEntity<List<UserResponse>> searchUser(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role
    ){
        return ResponseEntity.ok(userService.searchUsers(name, role));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserRequest req) {

        return ResponseEntity.ok(userService.create(req));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {

        passwordResetTokenService.forgotPassword(email);

        return ResponseEntity.ok(
                Map.of("message", "Email đặt lại mật khẩu đã được gửi")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        passwordResetTokenService.resetPassword(token, newPassword);

        return ResponseEntity.ok(
                Map.of("message", "Đặt lại mật khẩu thành công")
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UserRequest req) {

        return ResponseEntity.ok(userService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
