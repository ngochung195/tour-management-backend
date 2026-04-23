package com.example.tour_management.repository;

import com.example.tour_management.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    boolean existsByCode(String code);

    java.util.Optional<Promotion> findByCode(String code);

    @Query("""
        SELECT p FROM Promotion p
        WHERE (:code IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%')))
        AND (:startDate IS NULL OR p.startDate >= :startDate)
        AND (:endDate IS NULL OR p.endDate <= :endDate)
        AND (:status IS NULL OR p.isActive = :status)
    """)
    List<Promotion> search(
            @Param("code") String code,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") Boolean status
    );
}