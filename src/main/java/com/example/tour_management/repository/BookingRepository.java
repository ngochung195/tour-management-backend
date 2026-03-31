package com.example.tour_management.repository;

import com.example.tour_management.entity.Booking;
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
}
