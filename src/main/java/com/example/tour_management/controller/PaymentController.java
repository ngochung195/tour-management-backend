package com.example.tour_management.controller;

import com.example.tour_management.dto.PaymentResponse;
import com.example.tour_management.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/create_payment")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestParam Integer bookingId,
            @RequestParam(required = false) String bankCode,
            HttpServletRequest request) {

        PaymentResponse response = paymentService.createPayment(bookingId, bankCode, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay_return")
    public void vnpayReturn(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {

        String redirectUrl = paymentService.handlePaymentReturn(request);
        response.sendRedirect(redirectUrl);
    }
}