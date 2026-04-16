package com.example.tour_management.repository;

import com.example.tour_management.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    List<Vehicle> findByVehicleName(String keyword);

}