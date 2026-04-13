package com.example.tour_management.service;

import com.example.tour_management.exception.BadRequestException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    // ================= BOOKING EMAIL =================
    @Async
    public void sendBookingConfirm(String to, String userName, String tourName,
                                   LocalDate startDate, LocalDate endDate, Integer quantity){

        log.info("Gửi mail xác nhận booking tới: {}", to);

        if (to == null || to.isBlank()) {
            throw new BadRequestException("Email người nhận không hợp lệ");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            message.setTo(to);
            message.setSubject("Xác nhận đặt tour: " + tourName);
            message.setText(
                    "Chào " + userName + ",\n" +
                            "Bạn đã đặt tour " + tourName + " thành công.\n" +
                            "Ngày khởi hành: " + startDate.format(formatter) + "\n" +
                            "Ngày kết thúc: " + endDate.format(formatter) + "\n" +
                            "Số lượng: " + quantity + "\n" +
                            "Cảm ơn bạn đã sử dụng TravelGo!"
            );

            mailSender.send(message);

            log.info("Gửi mail booking thành công tới: {}", to);

        } catch (Exception e) {
            log.error("Lỗi gửi mail booking tới {}: {}", to, e.getMessage());
        }
    }

    // ================= RESET PASSWORD =================
    @Async
    public void sendResetPasswordEmail(String toEmail, String link) {

        log.info("Gửi mail reset password tới: {}", toEmail);

        if (toEmail == null || toEmail.isBlank()) {
            throw new BadRequestException("Email không hợp lệ");
        }

        if (link == null || link.isBlank()) {
            throw new BadRequestException("Link reset không hợp lệ");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu tài khoản");

            String content =
                    "<div style='font-family:Arial,sans-serif'>"
                            + "<h2>Yêu cầu đặt lại mật khẩu</h2>"
                            + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu.</p>"
                            + "<p>Nhấn vào nút dưới đây để đổi mật khẩu:</p>"
                            + "<a href='" + link + "' "
                            + "style='padding:10px 20px;background:#007bff;color:white;text-decoration:none;border-radius:5px'>"
                            + "Đặt lại mật khẩu</a>"
                            + "<p style='margin-top:20px'>Link có hiệu lực trong 15 phút.</p>"
                            + "</div>";

            helper.setText(content, true);

            mailSender.send(message);

            log.info("Gửi mail reset password thành công tới: {}", toEmail);

        } catch (Exception e) {
            log.error("Lỗi gửi mail reset tới {}: {}", toEmail, e.getMessage());
            throw new BadRequestException("Không thể gửi email reset mật khẩu");
        }
    }
}