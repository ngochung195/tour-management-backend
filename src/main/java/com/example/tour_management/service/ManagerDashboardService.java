package com.example.tour_management.service;

import com.example.tour_management.dto.booking.BookingResponse;
import com.example.tour_management.dto.dashboard.*;
import com.example.tour_management.dto.review.ReviewResponse;
import com.example.tour_management.entity.Booking;
import com.example.tour_management.entity.Review;
import com.example.tour_management.entity.User;
import com.example.tour_management.enums.BookingStatus;
import com.example.tour_management.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ManagerDashboardService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    public ManagerDashboardService(
            BookingRepository bookingRepository,
            TourRepository tourRepository,
            ReviewRepository reviewRepository,
            UserService userService
    ) {
        this.bookingRepository = bookingRepository;
        this.tourRepository = tourRepository;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
    }

    public ManagerDashboardResponse getDashboard(Authentication authentication) {

        User manager = userService.getUserFromAuthentication(authentication);
        Integer managerId = manager.getId();

        ManagerDashboardResponse res = new ManagerDashboardResponse();

        BigDecimal revenue = bookingRepository.getManagerRevenue(managerId);
        res.setTotalRevenue(revenue != null ? revenue : BigDecimal.ZERO);

        res.setPendingBookings(
                bookingRepository.countPendingBookings(managerId)
        );

        res.setActiveTours(
                tourRepository.countActiveTours(managerId)
        );

        res.setAvgRating(
                reviewRepository.getAvgRating(managerId)
        );

        List<RevenueChartResponse> chart =
                bookingRepository.getRevenueByMonthForManager(
                                managerId,
                                List.of(BookingStatus.PAID, BookingStatus.APPROVED)
                        )
                        .stream()
                        .map(obj -> new RevenueChartResponse(
                                ((Number) obj[0]).intValue(),
                                (BigDecimal) obj[1]
                        ))
                        .toList();

        res.setRevenueChart(chart);

        List<BookingResponse> bookingList =
                bookingRepository.getRecentBookings(managerId, PageRequest.of(0, 5))
                        .stream()
                        .map(this::mapBookingToDTO)
                        .toList();

        res.setRecentBookings(bookingList);

        List<ReviewResponse> reviewList =
                reviewRepository.getRecentReviews(managerId, PageRequest.of(0, 5))
                        .stream()
                        .map(this::mapReviewToDTO)
                        .toList();

        res.setRecentReviews(reviewList);

        List<TopTourResponse> topTours =
                bookingRepository.getTopToursManager(managerId, PageRequest.of(0, 5));
        res.setTopTours(topTours);

        return res;
    }


    private BookingResponse mapBookingToDTO(Booking b) {

        BookingResponse dto = new BookingResponse();

        dto.setId(b.getId());
        dto.setTotal(b.getTotal());
        dto.setStatus(b.getStatus().name());
        dto.setBookingDate(b.getBookingDate());
        dto.setBookingCode(b.getBookingCode());
        dto.setQuantity(b.getQuantity());

        if (b.getTour() != null) {
            dto.setTourId(b.getTour().getId());
            dto.setTourName(b.getTour().getTourName());
        }

        if (b.getUser() != null) {
            dto.setUserId(b.getUser().getId());
            dto.setUserName(b.getUser().getUserName());
        }

        return dto;
    }

    private ReviewResponse mapReviewToDTO(Review r) {

        ReviewResponse dto = new ReviewResponse();

        dto.setId(r.getId());
        dto.setRating(r.getRating());
        dto.setReviewText(r.getReviewText());

        if (r.getTour() != null) {
            dto.setTourId(r.getTour().getId());
            dto.setTourName(r.getTour().getTourName());
        }

        if (r.getUser() != null) {
            dto.setUserId(r.getUser().getId());
            dto.setUserName(r.getUser().getUserName());
        }

        return dto;
    }
}