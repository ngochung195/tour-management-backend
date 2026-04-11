package com.example.tour_management.consumer;

import com.example.tour_management.dto.email.EmailMessage;
import com.example.tour_management.dto.email.ResetPasswordEmailMessage;
import com.example.tour_management.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {
    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = "emailQueue")
    public void receive(EmailMessage msg){

        System.out.println("Received message from RabbitMQ");

        System.out.println("Đang gửi email tới: " + msg.getTo());

        emailService.sendBookingConfirm(
                msg.getTo(),
                msg.getUserName(),
                msg.getTourName(),
                msg.getStartDate(),
                msg.getEndDate(),
                msg.getQuantity()
        );
    }

    @RabbitListener(queues = "email.reset.queue")
    public void handleResetPasswordEmail(ResetPasswordEmailMessage message) {

        emailService.sendResetPasswordEmail(
                message.getEmail(),
                message.getLink()
        );
    }
}
