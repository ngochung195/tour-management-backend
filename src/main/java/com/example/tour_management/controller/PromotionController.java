package com.example.tour_management.controller;

import com.example.tour_management.dto.promotion.PromotionRequest;
import com.example.tour_management.dto.promotion.PromotionResponse;
import com.example.tour_management.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAll() {
        return ResponseEntity.ok(promotionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PromotionResponse>> search(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Boolean status
    ) {
        return ResponseEntity.ok(
                promotionService.search(code, startDate, endDate, status)
        );
    }

    @PostMapping
    public ResponseEntity<PromotionResponse> create(
            @Valid @RequestBody PromotionRequest request) {

        return ResponseEntity.ok(promotionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionRequest request) {

        return ResponseEntity.ok(promotionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        promotionService.delete(id);
        return ResponseEntity.ok("Xóa thành công");
    }
}