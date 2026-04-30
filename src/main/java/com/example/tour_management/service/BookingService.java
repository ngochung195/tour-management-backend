package com.example.tour_management.service;

import com.example.tour_management.dto.booking.*;
import com.example.tour_management.dto.email.EmailMessage;
import com.example.tour_management.dto.revenue.RevenueResponse;
import com.example.tour_management.entity.*;
import com.example.tour_management.enums.BookingStatus;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log =
            LoggerFactory.getLogger(BookingService.class);

    private static final String EMAIL_EXCHANGE = "emailExchange";
    private static final String EMAIL_ROUTING_KEY = "email.routing";

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final PromotionRepository promotionRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            TourRepository tourRepository,
            RedisTemplate<String, Object> redisTemplate,
            RabbitTemplate rabbitTemplate,
            PromotionRepository promotionRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.promotionRepository = promotionRepository;
    }

    public List<BookingResponse> getAll() {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new BadRequestException("Chưa đăng nhập");
        }

        String role = currentUser.getRole().getRoleName();

        if (role.startsWith("ROLE_")) role = role.substring(5);

        if ("ADMIN".equals(role)) {
            log.info("Admin lấy tất cả booking");
            return bookingRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        if ("MANAGER".equals(role)) {
            log.info("Manager {} lấy booking theo tour của mình", currentUser.getEmail());
            return bookingRepository.findByManagerId(currentUser.getId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        throw new BadRequestException("Không có quyền xem booking");
    }

    public BookingResponse getById(Integer id) {
        log.info("Lấy booking theo id={}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy booking id={}", id);
                    return new NotFoundException("Booking not found");
                });

        return toResponse(booking);
    }

    public List<BookingResponse> getByEmail(String email) {

        log.info("Lấy booking theo email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user email={}", email);
                    return new NotFoundException("User not found");
                });

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse create(BookingRequest req, String email) {

        log.info("Tạo booking - email={}, tourId={}, quantity={}",
                email, req.getTourId(), req.getQuantity());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Tour tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new NotFoundException("Tour not found"));

        int quantity = req.getQuantity();

        if (tour.getQuantity() < quantity) {
            log.warn("Không đủ chỗ. tourId={}, available={}, requested={}",
                    tour.getId(), tour.getQuantity(), quantity);
            throw new BadRequestException("Số lượng tour không đủ");
        }

        redisTemplate.delete("tours");
        redisTemplate.delete("tour:" + tour.getId());

        BigDecimal unitPrice = tour.getPrice();
        LocalDateTime now = LocalDateTime.now();

        String promoCode = req.getPromotionCode();
        if (promoCode != null && !promoCode.isBlank()) {

            Optional<Promotion> promoOpt =
                    promotionRepository.findByCode(promoCode.trim().toUpperCase());

            if (promoOpt.isPresent()) {
                Promotion promo = promoOpt.get();

                if (Boolean.TRUE.equals(promo.getActive())
                        && !now.isBefore(promo.getStartDate())
                        && !now.isAfter(promo.getEndDate())) {

                    BigDecimal discountRate =
                            promo.getDiscount().divide(BigDecimal.valueOf(100));

                    unitPrice = unitPrice.multiply(
                            BigDecimal.ONE.subtract(discountRate)
                    );

                    log.info("Áp mã giảm giá: code={}, discount={}%",
                            promoCode, promo.getDiscount());
                } else {
                    log.warn("Mã không hợp lệ hoặc hết hạn: {}", promoCode);
                }
            } else {
                log.warn("Không tìm thấy mã: {}", promoCode);
            }
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTour(tour);
        booking.setQuantity(quantity);
        booking.setTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingDate(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        saved.setBookingCode(String.format("BK-%03d", saved.getId()));
        bookingRepository.save(saved);

        log.info("Tạo booking thành công. id={}, code={}",
                saved.getId(), saved.getBookingCode());

        return toResponse(saved);
    }

    @Transactional
    public void handlePaymentCallback(Integer bookingId, String status) {

        log.info("Payment callback - bookingId={}, status={}",
                bookingId, status);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Booking không ở trạng thái PENDING. id={}", bookingId);
            return;
        }

        if ("success".equals(status)) {

            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);

            log.info("Thanh toán thành công. bookingId={}", bookingId);

            handleAfterPayment(booking);

        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            log.warn("Thanh toán thất bại. bookingId={}", bookingId);
        }
    }

    @Transactional
    public void handleAfterPayment(Booking booking) {

        Tour tour = booking.getTour();

        if (tour.getQuantity() < booking.getQuantity()) {
            throw new BadRequestException("Tour đã hết chỗ");
        }

        tour.setQuantity(
                tour.getQuantity() - booking.getQuantity()
        );

        redisTemplate.delete("tours");
        redisTemplate.delete("tour:" + tour.getId());

        EmailMessage msg = new EmailMessage();
        msg.setTo(booking.getUser().getEmail());
        msg.setUserName(booking.getUser().getUserName());
        msg.setTourName(tour.getTourName());
        msg.setStartDate(tour.getStartDate());
        msg.setEndDate(tour.getEndDate());
        msg.setQuantity(booking.getQuantity());

        rabbitTemplate.convertAndSend(
                EMAIL_EXCHANGE,
                EMAIL_ROUTING_KEY,
                msg
        );

        log.info("Đã gửi email message sang RabbitMQ. bookingId={}",
                booking.getId());
    }

    @Scheduled(fixedRate = 300000)
    public void autoCancelBooking() {

        log.info("Chạy job auto cancel booking");

        LocalDateTime time = LocalDateTime.now().minusMinutes(15);

        List<Booking> expired = bookingRepository.findExpired(time);

        for (Booking b : expired) {
            log.info("Auto cancel bookingId={}", b.getId());
            b.setStatus(BookingStatus.CANCELLED);
        }

        bookingRepository.saveAll(expired);

        log.info("Tổng booking bị hủy: {}", expired.size());
    }

    @Transactional
    public void cancel(Integer id) {

        log.info("Hủy booking id={}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new BadRequestException("Tour đã được xác nhận, không thể hủy");
        }

        if (booking.getStatus() == BookingStatus.PAID) {

            Tour tour = booking.getTour();

            tour.setQuantity(
                    tour.getQuantity() + booking.getQuantity()
            );

            redisTemplate.delete("tours");
            redisTemplate.delete("tour:" + tour.getId());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public BookingResponse updateStatus(Integer id, String status) {

        log.info("Cập nhật trạng thái booking id={}, status={}", id, status);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        BookingStatus newStatus;

        try {
            newStatus = BookingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.error("Status không hợp lệ: {}", status);
            throw new BadRequestException("Trạng thái không hợp lệ");
        }

        if ((booking.getStatus() == BookingStatus.PAID
                || booking.getStatus() == BookingStatus.APPROVED)
                && (newStatus == BookingStatus.REJECTED
                || newStatus == BookingStatus.CANCELLED)) {

            Tour tour = booking.getTour();

            tour.setQuantity(
                    tour.getQuantity() + booking.getQuantity()
            );

            redisTemplate.delete("tours");
            redisTemplate.delete("tour:" + tour.getId());
        }

        booking.setStatus(newStatus);

        return toResponse(bookingRepository.save(booking));
    }

    public void delete(Integer id) {

        log.warn("Xóa booking id={}", id);

        if (!bookingRepository.existsById(id)) {
            throw new NotFoundException("Booking not found");
        }

        bookingRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    public List<RevenueResponse> getRevenueByMonth() {
        return bookingRepository.getRevenueByMonth();
    }

    public List<RevenueResponse> getRevenueByQuarter() {
        return bookingRepository.getRevenueByQuarter();
    }

    private BookingResponse toResponse(Booking b) {

        BookingResponse res = new BookingResponse();

        res.setId(b.getId());

        if (b.getUser() != null) {
            res.setUserId(b.getUser().getId());
            res.setUserName(b.getUser().getUserName());
        }

        if (b.getTour() != null) {
            res.setTourId(b.getTour().getId());
            res.setTourName(b.getTour().getTourName());
        }

        res.setQuantity(b.getQuantity());
        res.setTotal(b.getTotal());
        res.setStatus(b.getStatus().name());
        res.setBookingDate(b.getBookingDate());
        res.setBookingCode(b.getBookingCode());

        return res;
    }
}