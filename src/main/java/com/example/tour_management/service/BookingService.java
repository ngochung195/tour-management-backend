package com.example.tour_management.service;

    import com.example.tour_management.dto.email.EmailMessage;
import com.example.tour_management.dto.booking.BookingRequest;
import com.example.tour_management.dto.booking.BookingResponse;
import com.example.tour_management.entity.Booking;
import com.example.tour_management.entity.Tour;
import com.example.tour_management.entity.User;
import com.example.tour_management.enums.BookingStatus;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.BookingRepository;
import com.example.tour_management.repository.TourRepository;
import com.example.tour_management.repository.UserRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // user
    public List<BookingResponse> getAll() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BookingResponse getById(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        return toResponse(booking);
    }

    public List<BookingResponse> getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse create(BookingRequest req, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Tour tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new NotFoundException("Tour not found"));

        int quantity = req.getQuantity();

        if (tour.getQuantity() < quantity) {
            throw new RuntimeException("Not enough tour quantity");
        }

        redisTemplate.delete("tours");
        redisTemplate.delete("tour:" + tour.getId());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setTour(tour);
        booking.setQuantity(quantity);
        booking.setTotal(
                tour.getPrice().multiply(
                        java.math.BigDecimal.valueOf(quantity)
                )
        );
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingDate(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        saved.setBookingCode(String.format("BK-%03d", saved.getId()));
        bookingRepository.save(saved);

        return toResponse(saved);
    }


    @Transactional
    public void handlePaymentCallback(Integer bookingId, String status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }

        if ("success".equals(status)) {

            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);

            handleAfterPayment(booking);

        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }

    @Transactional
    public void handleAfterPayment(Booking booking) {

        Tour tour = booking.getTour();

        if (tour.getQuantity() < booking.getQuantity()) {
            throw new RuntimeException("Tour is full");
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
                "emailExchange",
                "email.routing",
                msg
        );

        System.out.println("Sent to RabbitMQ");

    }
    @Scheduled(fixedRate = 300000)
    public void autoCancelBooking() {

        LocalDateTime time = LocalDateTime.now().minusMinutes(15);

        List<Booking> expired = bookingRepository.findExpired(time);

        for (Booking b : expired) {
            b.setStatus(BookingStatus.CANCELLED);
        }
        System.out.println("Auto cancelled: " + expired.size());

        bookingRepository.saveAll(expired);
    }


    @Transactional
    public void cancel(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.PAID
                || booking.getStatus() == BookingStatus.APPROVED) {

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

    // manager, admin

    @Transactional
    public BookingResponse updateStatus(Integer id, String status) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        BookingStatus newStatus;

        try {
            newStatus = BookingStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid booking status: " + status);
        }

        if ((booking.getStatus() == BookingStatus.PAID || booking.getStatus() == BookingStatus.APPROVED)
                        && (newStatus == BookingStatus.REJECTED || newStatus == BookingStatus.CANCELLED)){

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
        if (!bookingRepository.existsById(id)) {
            throw new NotFoundException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    // mapping
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
