package com.example.tour_management.service;

import com.example.tour_management.dto.email.ResetPasswordEmailMessage;
import com.example.tour_management.entity.PasswordResetToken;
import com.example.tour_management.entity.User;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.PasswordResetTokenRepository;
import com.example.tour_management.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // FORGOT PASSWORD
    public void forgotPassword(String email) {

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email không được để trống");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Email không đúng định dạng");
        }

        if (!userRepository.existsByEmail(email)) {
            throw new NotFoundException("Email không tồn tại");
        }

        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);

        String link = "http://localhost:4200/reset-password?token=" + token;

        ResetPasswordEmailMessage message =
                new ResetPasswordEmailMessage(email, link);

        rabbitTemplate.convertAndSend(
                "email.exchange",
                "email.reset",
                message
        );
    }

    // RESET PASSWORD
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token không hợp lệ"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token đã hết hạn");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}