package com.example.tour_management.service;

import com.example.tour_management.dto.vehicle.VehicleRequest;
import com.example.tour_management.dto.vehicle.VehicleResponse;
import com.example.tour_management.entity.Vehicle;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<VehicleResponse> getAll() {
        return vehicleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VehicleResponse getById(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phương tiện"));

        return mapToResponse(vehicle);
    }

    public List<VehicleResponse> search(String keyword) {
        List<Vehicle> list;

        if (keyword == null || keyword.isBlank()) {
            list = vehicleRepository.findAll();
        } else {
            list = vehicleRepository.findByVehicleName(keyword);
        }

        return list.stream().map(this::mapToResponse).toList();
    }

    public VehicleResponse create(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();

        vehicle.setVehicleName(request.getVehicleName());
        vehicle.setDescription(request.getDescription());

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    public VehicleResponse update(Integer id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phương tiện"));

        vehicle.setVehicleName(request.getVehicleName());
        vehicle.setDescription(request.getDescription());

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    public void delete(Integer id) {
        if (!vehicleRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy phương tiện");
        }

        vehicleRepository.deleteById(id);
    }

    private VehicleResponse mapToResponse(Vehicle v) {
        VehicleResponse res = new VehicleResponse();

        res.setId(v.getId());
        res.setVehicleName(v.getVehicleName());
        res.setDescription(v.getDescription());

        return res;
    }
}