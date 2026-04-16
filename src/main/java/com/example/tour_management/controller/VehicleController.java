package com.example.tour_management.controller;

import com.example.tour_management.dto.vehicle.VehicleRequest;
import com.example.tour_management.dto.vehicle.VehicleResponse;
import com.example.tour_management.service.VehicleService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAll() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(vehicleService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponse>> search(
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(vehicleService.search(keyword));
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity.ok(vehicleService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        vehicleService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}