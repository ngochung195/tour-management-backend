package com.example.tour_management.controller;

import com.example.tour_management.dto.PaymentResponse;
import com.example.tour_management.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/create_payment")
    public ResponseEntity<?> createPayment(
            @RequestParam Integer bookingId,
            @RequestParam(required = false) String bankCode,
            HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createPaymentUrl(bookingId, bankCode, request);

            PaymentResponse response = new PaymentResponse();
            response.setStatus("OK");
            response.setMessage("Successfully");
            response.setURL(paymentUrl);
            response.setBookingId(String.valueOf(bookingId));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PaymentResponse response = new PaymentResponse();
            response.setStatus("ERROR");
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/vnpay_return")
    public void vnpayReturn(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        boolean success = paymentService.processPaymentReturn(params);

        String redirectUrl = success
                ? "http://localhost:4200/payment-result?status=success"
                : "http://localhost:4200/payment-result?status=fail";

        response.sendRedirect(redirectUrl);
    }
}