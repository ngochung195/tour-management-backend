package com.example.tour_management.repository;

import com.example.tour_management.dto.booking.BookingResponse;
import com.example.tour_management.dto.dashboard.RevenueChartResponse;
import com.example.tour_management.dto.dashboard.TopTourResponse;
import com.example.tour_management.dto.revenue.RevenueResponse;
import com.example.tour_management.entity.Booking;
import com.example.tour_management.enums.BookingStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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

    @Query("SELECT b FROM Booking b WHERE b.tour.managerId = :managerId")
    List<Booking> findByManagerId(@Param("managerId") Integer managerId);

    @Query(value = """
                SELECT COUNT(*)
                FROM bookings
            """, nativeQuery = true)
    long countBookings();
    @Query("""
            SELECT new com.example.tour_management.dto.dashboard.RevenueChartResponse(
                MONTH(b.bookingDate),
                SUM(b.total)
            )
            FROM Booking b
            WHERE b.status IN ('PAID','APPROVED')
            GROUP BY MONTH(b.bookingDate)
            ORDER BY MONTH(b.bookingDate)
        """)
    List<RevenueChartResponse> getRevenueChart();

    @Query("""
                SELECT new com.example.tour_management.dto.dashboard.TopTourResponse(
                    b.tour.id,
                    b.tour.tourName,
                    COUNT(b),
                    SUM(b.total)
                )
                FROM Booking b
                WHERE b.status IN ('PAID','APPROVED')
                GROUP BY b.tour.id, b.tour.tourName
                ORDER BY COUNT(b) DESC
            """)
    List<TopTourResponse> getTopTours(Pageable pageable);

    @Query("""
                    SELECT COALESCE(SUM(b.total),0)
                    FROM Booking b
                    JOIN b.tour t
                    WHERE t.managerId = :managerId
                    AND b.status IN ('PAID','APPROVED')
            """)
    BigDecimal getManagerRevenue(@Param("managerId") Integer managerId);


    @Query("""
                    SELECT COUNT(b)
                    FROM Booking b
                    JOIN b.tour t
                    WHERE t.managerId = :managerId
                    AND b.status IN ('PENDING', 'PAID')
            """)
    long countPendingBookings(@Param("managerId") Integer managerId);
                @Query("""
                SELECT MONTH(b.bookingDate), SUM(b.total)
                FROM Booking b
                JOIN b.tour t
                WHERE t.managerId = :managerId
                AND b.status IN :statuses
                GROUP BY MONTH(b.bookingDate)
                ORDER BY MONTH(b.bookingDate)
            """)
    List<Object[]> getRevenueByMonthForManager(
            @Param("managerId") Integer managerId,
            @Param("statuses") List<BookingStatus> statuses
    );

    @Query("""
                SELECT b
                FROM Booking b
                JOIN b.tour t
                JOIN b.user u
                WHERE t.managerId = :managerId
                ORDER BY b.bookingDate DESC
            """)
    List<Booking> getRecentBookings(@Param("managerId") Integer managerId, Pageable pageable);

    @Query("""
                SELECT new com.example.tour_management.dto.dashboard.TopTourResponse(
                    b.tour.id,
                    b.tour.tourName,
                    COUNT(b),
                    SUM(b.total)
                )
                FROM Booking b
                WHERE b.status IN ('PAID','APPROVED')
                AND b.tour.managerId = :managerId
                GROUP BY b.tour.id, b.tour.tourName
                ORDER BY COUNT(b) DESC
            """)
    List<TopTourResponse> getTopToursManager(@Param("managerId") Integer managerId, Pageable pageable);

    @Query("""
                SELECT b FROM Booking b
                JOIN FETCH b.user
                JOIN FETCH b.tour
                WHERE b.status = 'PAID'
                ORDER BY b.bookingDate DESC
            """)
    List<Booking> findRecentPaidBookings(Pageable pageable);

    @Query("""
                SELECT b FROM Booking b
                JOIN FETCH b.user
                JOIN FETCH b.tour
                WHERE b.status = 'CANCELLED'
                ORDER BY b.bookingDate DESC
            """)
    List<Booking> findRecentCancelledBookings(Pageable pageable);

}
