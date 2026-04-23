package com.example.tour_management.repository;

import com.example.tour_management.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByTourId(Integer tourId);
}