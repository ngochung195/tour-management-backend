package com.example.tour_management.repository;

import com.example.tour_management.entity.Tour;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Integer> {

    @Query(value = """
                SELECT *
                FROM tours
                WHERE (:keyword IS NULL OR tour_name LIKE %:keyword%)
                AND (:startDate IS NULL OR start_date >= :startDate)
                AND (:endDate IS NULL OR end_date <= :endDate)
                AND (:categoryId IS NULL OR category_id = :categoryId)
                AND (:managerId IS NULL OR manager_id = :managerId)
            """, nativeQuery = true)
    List<Tour> searchTour(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Integer categoryId,
            @Param("managerId") Integer managerId
    );

        @Query(value = """
        SELECT *
        FROM tours
        WHERE (:keyword IS NULL OR tour_name LIKE %:keyword%)
        AND (:startDate IS NULL OR start_date >= :startDate)
        AND (:endDate IS NULL OR end_date <= :endDate)
        AND (:categoryId IS NULL OR category_id = :categoryId)
        AND manager_id = :managerId
    """, nativeQuery = true)
    List<Tour> searchTourByManager(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Integer categoryId,
            @Param("managerId") Integer managerId
    );

    List<Tour> findByManagerId(Integer managerId);

    @Query(value = """
            SELECT COUNT(*)
            FROM tours t
            WHERE t.manager_id = :managerId
        """, nativeQuery = true)
    long countActiveTours(@Param("managerId") Integer managerId);
}