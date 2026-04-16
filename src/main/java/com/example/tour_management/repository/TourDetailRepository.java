package com.example.tour_management.repository;

import com.example.tour_management.entity.TourDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourDetailRepository extends JpaRepository<TourDetail, Integer> {

    List<TourDetail> findByTourId(Integer tourId);

    List<TourDetail> findByHotelId(Integer hotelId);

    List<TourDetail> findByVehicleId(Integer vehicleId);
}