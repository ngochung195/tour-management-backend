package com.example.tour_management.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Async
    public void sendBookingConfirm(String to, String userName, String tourName,
                                   LocalDate startDate, LocalDate endDate, Integer quantity){
        SimpleMailMessage message = new SimpleMailMessage();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        message.setTo(to);
        message.setSubject("Xác nhận đặt tour: " + tourName);
        message.setText("Chào "+ userName + ",\n" +
                        "Bạn đã đặt tour " + tourName + " thành công.\n" +
                        "Ngày khởi hành: " + startDate.format(formatter) + ".\n" +
                        "Ngày kết thúc: " + endDate.format(formatter) + ".\n" +
                        "Số lượng: " + quantity + ".\n" +
                        "Cảm ơn bạn đã sử dụng TraveGo!");
        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String toEmail, String link) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset mật khẩu tài khoản");

            String content =
                            "<h2>Yêu cầu đặt lại mật khẩu</h2>"
                            + "<p>Chúng tôi nhận được yêu cầu reset mật khẩu.</p>"
                            + "<p>Nhấn vào link dưới đây để đổi mật khẩu:</p>"
                            + "<a href='" + link + "'>Reset Password</a>"
                            + "<br><p>Link có hiệu lực 15 phút.</p>";

            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Send reset email failed: " + e.getMessage());
        }
    }
}
