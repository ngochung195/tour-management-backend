package com.example.tour_management.service;

import com.example.tour_management.config.VNPayConfig;
import com.example.tour_management.dto.PaymentResponse;
import com.example.tour_management.entity.Booking;
import com.example.tour_management.enums.BookingStatus;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.BookingRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Value("${app.fe-url}")
    private String feUrl;

    public PaymentService(BookingRepository bookingRepository,
                          BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    public PaymentResponse createPayment(Integer bookingId, String bankCode,
                                         HttpServletRequest request) {

        log.info("Tạo payment cho bookingId={}", bookingId);

        try {
            String paymentUrl = createPaymentUrl(bookingId, bankCode, request);

            PaymentResponse response = new PaymentResponse();
            response.setStatus("OK");
            response.setMessage("Tạo link thanh toán thành công");
            response.setURL(paymentUrl);
            response.setBookingId(String.valueOf(bookingId));

            return response;

        } catch (Exception e) {
            log.error("Lỗi tạo payment bookingId={}", bookingId, e);
            throw new BadRequestException("Không thể tạo thanh toán");
        }
    }

    public String createPaymentUrl(Integer bookingId, String bankCode,
                                   HttpServletRequest request)
            throws UnsupportedEncodingException {

        log.info("Tạo URL thanh toán cho bookingId={}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy booking id={}", bookingId);
                    return new NotFoundException("Không tìm thấy booking");
                });

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking không ở trạng thái chờ thanh toán");
        }

        if (booking.getTotal() == null || booking.getTotal().doubleValue() <= 0) {
            throw new BadRequestException("Số tiền không hợp lệ");
        }

        long amount = booking.getTotal().longValue();
        String vnp_TxnRef = booking.getBookingCode();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode",
                (bankCode == null || bankCode.isBlank()) ? "NCB" : bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan booking:" + bookingId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {

                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String secureHash = VNPayConfig.hmacSHA512(
                VNPayConfig.secretKey, hashData.toString());

        query.append("&vnp_SecureHash=").append(secureHash);

        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + query;

        log.info("Tạo URL thành công bookingId={}", bookingId);

        return paymentUrl;
    }

    public String handlePaymentReturn(HttpServletRequest request) {

        Map<String, String> params = extractParams(request);

        boolean success = processPaymentReturn(params);

        String redirectUrl = success
                ? feUrl + "/payment-result?status=success"
                : feUrl + "/payment-result?status=fail";

        log.info("VNPay return success={}, redirect={}", success, redirectUrl);

        return redirectUrl;
    }

    private Map<String, String> extractParams(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();

        request.getParameterMap().forEach((k, v) -> {
            if (v.length > 0) {
                params.put(k, v[0]);
            }
        });

        return params;
    }

    public boolean processPaymentReturn(Map<String, String> params) {

        String vnp_SecureHash = params.remove("vnp_SecureHash");

        if (vnp_SecureHash == null) {
            log.error("Thiếu chữ ký VNPay");
            return false;
        }

        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {

            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                } catch (Exception e) {
                    log.error("Lỗi encode param {}", fieldName);
                }

                if (itr.hasNext()) hashData.append('&');
            }
        }

        String calculatedHash = VNPayConfig.hmacSHA512(
                VNPayConfig.secretKey, hashData.toString());

        if (!calculatedHash.equalsIgnoreCase(vnp_SecureHash)) {
            log.error("Sai chữ ký VNPay");
            return false;
        }

        String responseCode = params.get("vnp_ResponseCode");
        String bookingCode = params.get("vnp_TxnRef");

        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bookingCode={}", bookingCode);
                    return new NotFoundException("Booking không tồn tại");
                });

        String status = "00".equals(responseCode) ? "success" : "fail";

        bookingService.handlePaymentCallback(booking.getId(), status);

        log.info("Thanh toán xong bookingCode={}, status={}", bookingCode, status);

        return "success".equals(status);
    }
}