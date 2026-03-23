package com.example.tour_management.repository;

import com.example.tour_management.entity.Tour;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public interface TourRepository extends JpaRepository<Tour, Integer> {
    @Query(value = """
        SELECT *
        FROM tours
        WHERE (:keyword IS NULL OR tour_name LIKE %:keyword%)
        AND (:startDate IS NULL OR start_Date = :startDate)
        AND (:endDate IS NULL OR end_Date = :endDate)
    """, nativeQuery = true)
    List<Tour> searchTour(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
            );

    @Query("""
        SELECT t FROM Tour t
        WHERE (:keyword IS NULL OR t.tourName LIKE CONCAT('%', :keyword, '%') )
        AND (:startDate IS NULL OR t.startDate = :startDate)
        AND (:endDate IS NULL OR t.endDate = :endDate)     
        AND (:categoryId IS NULL OR t.category.id = :categoryId)  
    """)
    List<Tour> searchPublic(
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId
    );
}
