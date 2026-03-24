package com.example.tour_management.controller;

import com.example.tour_management.dto.tour.TourRequest;
import com.example.tour_management.dto.tour.TourResponse;
import com.example.tour_management.service.TourService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService tourService;

    // customer
    @GetMapping
    public ResponseEntity<List<TourResponse>> getAll() {
        return ResponseEntity.ok(tourService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(tourService.getById(id));
    }

    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<List<TourResponse>>> searchPublic(
            @RequestParam(required = false)String keyword,
            @RequestParam(required = false)LocalDate startDate,
            @RequestParam(required = false)LocalDate endDate,
            @RequestParam(required = false)Long categoryId
            ){
        return tourService.searchTourPublicAsync(keyword, startDate, endDate, categoryId)
                .thenApply(result -> ResponseEntity.ok(result));
    }

    // manager, admin
    @GetMapping("/search-tour")
    public ResponseEntity<List<TourResponse>> searchTour(
            @RequestParam(required = false)String keyword,
            @RequestParam(required = false)LocalDate startDate,
            @RequestParam(required = false)LocalDate endDate
            ){
        return ResponseEntity.ok(tourService.searchTour(keyword, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<TourResponse> create(
            @ModelAttribute TourRequest req) {

        return ResponseEntity.ok(tourService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponse> update(
            @PathVariable Integer id,
            @ModelAttribute TourRequest req) {

        return ResponseEntity.ok(tourService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        tourService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}