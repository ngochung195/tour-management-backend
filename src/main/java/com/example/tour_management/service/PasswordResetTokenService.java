package com.example.tour_management.service;

import com.example.tour_management.dto.email.ResetPasswordEmailMessage;
import com.example.tour_management.entity.PasswordResetToken;
import com.example.tour_management.entity.User;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.PasswordResetTokenRepository;
import com.example.tour_management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    private static final Logger log =
            LoggerFactory.getLogger(PasswordResetTokenService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.fe-url}")
    private String feUrl;

    public PasswordResetTokenService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            RabbitTemplate rabbitTemplate
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public void forgotPassword(String email) {

        log.info("Yêu cầu quên mật khẩu với email: {}", email);

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email không được để trống");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Email không đúng định dạng");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email không tồn tại"));

        passwordResetTokenRepository.findByEmail(email).ifPresent(existing -> {
            if (existing.getExpiryDate().isAfter(LocalDateTime.now())) {
                throw new BadRequestException("Vui lòng đợi trước khi gửi lại yêu cầu");
            }
        });

        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        passwordResetTokenRepository.save(resetToken);

        String link = feUrl + "/reset-password?token=" + token;

        ResetPasswordEmailMessage message =
                new ResetPasswordEmailMessage(email, link);

        try {
            rabbitTemplate.convertAndSend(
                    "email.exchange",
                    "email.reset",
                    message
            );

            log.info("Đã gửi message reset password vào queue cho email: {}", email);

        } catch (Exception e) {
            log.error("Lỗi gửi RabbitMQ: {}", e.getMessage());
            throw new BadRequestException("Không thể gửi email reset mật khẩu");
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {

        log.info("Thực hiện reset password với token");

        if (token == null || token.isBlank()) {
            throw new BadRequestException("Token không hợp lệ");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("Token không tồn tại");
                    return new BadRequestException("Token không hợp lệ");
                });

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Token đã hết hạn");
            throw new BadRequestException("Token đã hết hạn");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new NotFoundException("User không tồn tại"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        log.info("Reset password thành công cho email: {}", user.getEmail());
    }
}