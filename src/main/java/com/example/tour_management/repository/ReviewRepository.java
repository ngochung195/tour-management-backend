package com.example.tour_management.repository;

import com.example.tour_management.dto.review.ReviewResponse;
import com.example.tour_management.entity.Review;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByTourId(Integer tourId);

    @Query("""
                SELECT AVG(r.rating)
                FROM Review r
                JOIN r.tour t
                WHERE t.managerId = :managerId
            """)
    Double getAvgRating(@Param("managerId") Integer managerId);

    @Query("""
                SELECT r
                FROM Review r
                JOIN r.tour t
                WHERE t.managerId = :managerId
                ORDER BY r.id DESC
            """)
    List<Review> getRecentReviews(@Param("managerId") Integer managerId, Pageable pageable);
}