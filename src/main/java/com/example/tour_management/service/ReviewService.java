package com.example.tour_management.service;

import com.example.tour_management.dto.review.ReviewRequest;
import com.example.tour_management.dto.review.ReviewResponse;
import com.example.tour_management.entity.*;
import com.example.tour_management.enums.BookingStatus;
import com.example.tour_management.exception.BadRequestException;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private TourRepository tourRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;

    public ReviewResponse createReview(ReviewRequest req, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user"));

        Tour tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        boolean hasBooked = bookingRepository
                .existsByUserIdAndTourIdAndStatusIn(
                        user.getId(),
                        req.getTourId(),
                        List.of(
                            BookingStatus.PAID,
                            BookingStatus.APPROVED
                        )
                );

        if (!hasBooked) {
            throw new BadRequestException("Bạn chưa đặt tour này");
        }

        Review review = new Review();
        review.setUser(user);
        review.setTour(tour);
        review.setReviewText(req.getReviewText());
        review.setRating(req.getRating());

        reviewRepository.save(review);

        return mapToResponse(review);
    }

    public List<ReviewResponse> getByTour(Integer tourId) {
        return reviewRepository.findByTourId(tourId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ReviewResponse updateReview(Integer reviewId, ReviewRequest req, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy review"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền sửa review này");
        }

        review.setReviewText(req.getReviewText());
        review.setRating(req.getRating());

        return mapToResponse(reviewRepository.save(review));
    }

    public void deleteReview(Integer reviewId, String email, boolean isAdmin) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy review"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user"));

        if (isAdmin || review.getUser().getId().equals(user.getId())) {
            reviewRepository.delete(review);
        } else {
            throw new BadRequestException("Bạn không có quyền xóa review này");
        }
    }

    private ReviewResponse mapToResponse(Review r) {
        ReviewResponse res = new ReviewResponse();
        res.setId(r.getId());
        res.setUserId(r.getUser().getId());
        res.setUserName(r.getUser().getUserName());
        res.setTourId(r.getTour().getId());
        res.setTourName(r.getTour().getTourName());
        res.setReviewText(r.getReviewText());
        res.setRating(r.getRating());
        return res;
    }
}