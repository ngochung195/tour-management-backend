package com.example.tour_management.controller;

import com.example.tour_management.dto.tourdetail.TourDetailRequest;
import com.example.tour_management.dto.tourdetail.TourDetailResponse;
import com.example.tour_management.service.TourDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tour-details")
public class TourDetailController {

    @Autowired
    private TourDetailService tourDetailService;

    @GetMapping
    public ResponseEntity<List<TourDetailResponse>> getAll() {
        return ResponseEntity.ok(tourDetailService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(tourDetailService.getById(id));
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<TourDetailResponse>> getByTour(@PathVariable Integer tourId) {
        return ResponseEntity.ok(tourDetailService.getByTourId(tourId));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<TourDetailResponse>> getByHotel(@PathVariable Integer hotelId) {
        return ResponseEntity.ok(tourDetailService.getByHotelId(hotelId));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<TourDetailResponse>> getByVehicle(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(tourDetailService.getByVehicleId(vehicleId));
    }

    @PostMapping
    public ResponseEntity<TourDetailResponse> create(@RequestBody TourDetailRequest req) {
        return ResponseEntity.ok(tourDetailService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDetailResponse> update(
            @PathVariable Integer id,
            @RequestBody TourDetailRequest req) {
        return ResponseEntity.ok(tourDetailService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        tourDetailService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}