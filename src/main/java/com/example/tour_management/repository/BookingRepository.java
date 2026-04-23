package com.example.tour_management.repository;

import com.example.tour_management.dto.revenue.RevenueResponse;
import com.example.tour_management.entity.Booking;
import com.example.tour_management.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserId(Integer userId);

    @Query("SELECT b FROM  Booking  b WHERE b.status = 'PENDING' AND b.bookingDate < :time")
    List<Booking> findExpired(LocalDateTime time);

    Optional<Booking> findByBookingCode(String bookingCode);

    boolean existsByUserIdAndTourIdAndStatusIn(Integer userId, Integer tourId, List<BookingStatus> status);

    // DOANH THU THEO THÁNG
    @Query("""
                SELECT new com.example.tour_management.dto.revenue.RevenueResponse(
                    MONTH(b.bookingDate),
                    SUM(b.total)
                )
                FROM Booking b
                WHERE b.status = 'PAID' OR b.status = 'APPROVED'
                GROUP BY MONTH(b.bookingDate)
                ORDER BY MONTH(b.bookingDate)
            """)
    List<RevenueResponse> getRevenueByMonth();


    // DOANH THU THEO QUÝ
    @Query("""
                SELECT new com.example.tour_management.dto.revenue.RevenueResponse(
                    QUARTER(b.bookingDate),
                    SUM(b.total)
                )
                FROM Booking b
                WHERE b.status = 'PAID' OR b.status = 'APPROVED'
                GROUP BY QUARTER(b.bookingDate)
                ORDER BY QUARTER(b.bookingDate)
            """)
    List<RevenueResponse> getRevenueByQuarter();
}
