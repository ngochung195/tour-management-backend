package com.example.tour_management.repository;

import com.example.tour_management.entity.TourDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourDetailRepository extends JpaRepository<TourDetail, Integer> {

    List<TourDetail> findByTour_Id(Integer tourId);

    List<TourDetail> findByHotel_Id(Integer hotelId);

    List<TourDetail> findByVehicle_Id(Integer vehicleId);
}