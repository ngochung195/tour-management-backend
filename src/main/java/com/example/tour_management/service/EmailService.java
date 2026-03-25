package com.example.tour_management.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
}
