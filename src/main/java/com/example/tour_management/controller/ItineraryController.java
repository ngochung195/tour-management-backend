package com.example.tour_management.controller;

import com.example.tour_management.dto.itinerary.ItineraryRequest;
import com.example.tour_management.dto.itinerary.ItineraryResponse;
import com.example.tour_management.service.ItineraryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    @GetMapping
    public ResponseEntity<List<ItineraryResponse>> getAll() {
        return ResponseEntity.ok(itineraryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItineraryResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(itineraryService.getById(id));
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<ItineraryResponse>> getByTour(@PathVariable Integer tourId) {
        return ResponseEntity.ok(itineraryService.getByTourId(tourId));
    }

    @PostMapping
    public ResponseEntity<List<ItineraryResponse>> create(
            @RequestBody @Valid List<ItineraryRequest> requests
    ) {
        return ResponseEntity.ok(itineraryService.create(requests));
    }

    @PutMapping("/tour/{tourId}")
    public ResponseEntity<List<ItineraryResponse>> updateByTour(
            @PathVariable Integer tourId,
            @RequestBody @Valid List<ItineraryRequest> requests
    ) {
        return ResponseEntity.ok(itineraryService.update(tourId, requests));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        itineraryService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    @DeleteMapping("/tour/{tourId}")
    public ResponseEntity<String> deleteByTour(@PathVariable Integer tourId) {
        itineraryService.deleteByTour(tourId);
        return ResponseEntity.ok("Đã xóa toàn bộ lịch trình của tour");
    }
}