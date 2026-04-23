package com.example.tour_management.controller;

import com.example.tour_management.dto.booking.BookingRequest;
import com.example.tour_management.dto.booking.BookingResponse;
import com.example.tour_management.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // customer
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingRequest req,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(bookingService.create(req, email));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(bookingService.getByEmail(email));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Integer id)
    {
        bookingService.cancel(id);
        return ResponseEntity.ok().build();
    }

    //manager, admin
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings(){
        return ResponseEntity.ok(bookingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Integer id){
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ){
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Integer id){
        bookingService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // DOANH THU
    @GetMapping("/revenues/month")
    public ResponseEntity<?> getRevenueByMonth() {
        return ResponseEntity.ok(bookingService.getRevenueByMonth());
    }

    @GetMapping("/revenues/quarter")
    public ResponseEntity<?> getRevenueByQuarter() {
        return ResponseEntity.ok(bookingService.getRevenueByQuarter());
    }
}
