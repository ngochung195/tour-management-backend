package com.example.tour_management.controller;

import com.example.tour_management.dto.hotel.HotelRequest;
import com.example.tour_management.dto.hotel.HotelResponse;
import com.example.tour_management.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping
    public List<HotelResponse> getAll() {
        return hotelService.getAll();
    }

    @GetMapping("/{id}")
    public HotelResponse getById(@PathVariable Integer id) {
        return hotelService.getById(id);
    }

    @PostMapping
    public HotelResponse create(@Valid @RequestBody HotelRequest request) {
        return hotelService.create(request);
    }

    @PutMapping("/{id}")
    public HotelResponse update(@PathVariable Integer id,
                                @Valid @RequestBody HotelRequest request) {
        return hotelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        hotelService.delete(id);
        return "Deleted successfully";
    }
}
