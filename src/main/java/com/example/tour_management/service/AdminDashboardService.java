package com.example.tour_management.service;

import com.example.tour_management.dto.dashboard.*;
import com.example.tour_management.entity.Booking;
import com.example.tour_management.entity.Contact;
import com.example.tour_management.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final ContactRepository contactRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            TourRepository tourRepository,
            BookingRepository bookingRepository,
            ContactRepository contactRepository
    ) {
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.bookingRepository = bookingRepository;
        this.contactRepository = contactRepository;
    }

    public AdminDashboardResponse getDashboard() {

        AdminDashboardResponse res = new AdminDashboardResponse();

        res.setTotalUsers(userRepository.count());
        res.setTotalTours(tourRepository.count());
        res.setTotalBookings(bookingRepository.count());

        BigDecimal monthly = bookingRepository.getRevenueByMonth()
                .stream().map(r -> r.getRevenue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal quarterly = bookingRepository.getRevenueByQuarter()
                .stream().map(r -> r.getRevenue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        res.setMonthlyRevenue(monthly);
        res.setQuarterlyRevenue(quarterly);
        res.setRevenueChart(bookingRepository.getRevenueChart());

        List<TopTourResponse> topTours =
                bookingRepository.getTopTours(PageRequest.of(0, 5));
        enrichTopTours(topTours);
        res.setTopTours(topTours);

        res.setNewContacts(contactRepository.countNewContacts());

        res.setRecentActivities(buildRecentActivities());

        return res;
    }

    private void enrichTopTours(List<TopTourResponse> topTours) {
        for (TopTourResponse t : topTours) {
            tourRepository.findById(t.getTourId()).ifPresent(tour -> {

                if (tour.getCategory() != null) {
                    t.setLocation(tour.getCategory().getCategoryName()); // tuỳ tên getter trong Category entity
                }

                if (tour.getStartDate() != null) {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), tour.getStartDate());
                    t.setDaysUntilStart((int) Math.max(days, 0));
                } else {
                    t.setDaysUntilStart(0);
                }

                if (tour.getQuantity() != null && tour.getQuantity() > 0) {
                    long booked = t.getTotalBookings();
                    double ratio = (double) booked / tour.getQuantity();

                    if (ratio >= 1.0)      t.setStatus("CLOSED");
                    else if (ratio >= 0.8) t.setStatus("ALMOST_FULL");
                    else                   t.setStatus("OPEN");
                } else {
                    t.setStatus("OPEN");
                }
            });
        }
    }

    private List<RecentActivityResponse> buildRecentActivities() {
        List<Object[]> raw = new ArrayList<>(); // [RecentActivityResponse, LocalDateTime]

        List<Booking> paidBookings =
                bookingRepository.findRecentPaidBookings(PageRequest.of(0, 5));
        for (Booking b : paidBookings) {
            raw.add(new Object[]{
                    new RecentActivityResponse(
                            "BOOKING_NEW",
                            "Booking mới #" + b.getBookingCode() + " — " + b.getUser().getUserName(),
                            "Tour " + b.getTour().getTourName(),
                            calcTimeAgo(b.getBookingDate())
                    ),
                    b.getBookingDate()
            });
        }

        List<Booking> cancelledBookings =
                bookingRepository.findRecentCancelledBookings(PageRequest.of(0, 3));
        for (Booking b : cancelledBookings) {
            raw.add(new Object[]{
                    new RecentActivityResponse(
                            "BOOKING_CANCELLED",
                            "Booking #" + b.getBookingCode() + " bị huỷ",
                            b.getUser().getUserName() + " · Tour " + b.getTour().getTourName(),
                            calcTimeAgo(b.getBookingDate())
                    ),
                    b.getBookingDate()
            });
        }

        List<Contact> contacts =
                contactRepository.findRecentNewContacts(PageRequest.of(0, 3));
        for (Contact c : contacts) {
            raw.add(new Object[]{
                    new RecentActivityResponse(
                            "CONTACT",
                            "Liên hệ mới từ " + c.getUserName(),
                            c.getMessage() != null && c.getMessage().length() > 50
                                    ? c.getMessage().substring(0, 50) + "..."
                                    : c.getMessage(),
                            "Gần đây"
                    ),
                    LocalDateTime.MIN
            });
        }

        return raw.stream()
                .sorted((a, b) -> ((LocalDateTime) b[1]).compareTo((LocalDateTime) a[1]))
                .limit(8)
                .map(entry -> (RecentActivityResponse) entry[0])
                .collect(Collectors.toList());
    }

    private String calcTimeAgo(LocalDateTime time) {
        if (time == null) return "";
        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 1)   return "vừa xong";
        if (minutes < 60)  return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24)    return hours + " giờ trước";
        return (hours / 24) + " ngày trước";
    }
}