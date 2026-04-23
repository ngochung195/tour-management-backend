package com.example.tour_management.controller;

import com.example.tour_management.dto.review.ReviewRequest;
import com.example.tour_management.dto.review.ReviewResponse;
import com.example.tour_management.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ReviewResponse create(
            @RequestBody @Valid ReviewRequest req,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        return reviewService.createReview(req, user.getUsername());
    }

    @GetMapping("/tour/{tourId}")
    public List<ReviewResponse> getByTour(@PathVariable Integer tourId) {
        return reviewService.getByTour(tourId);
    }

    @PutMapping("/{id}")
    public ReviewResponse update(
            @PathVariable Integer id,
            @RequestBody ReviewRequest req,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        return reviewService.updateReview(id, req, user.getUsername());
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        boolean isAdmin = user.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        reviewService.deleteReview(id, user.getUsername(), isAdmin);
    }
}