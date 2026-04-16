package com.example.tour_management.repository;

import com.example.tour_management.entity.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {

    List<Itinerary> findByTourIdOrderByDayNumberAsc(Integer tourId);
}
