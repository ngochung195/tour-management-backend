package com.example.tour_management.service;

import com.example.tour_management.dto.tourdetail.TourDetailRequest;
import com.example.tour_management.dto.tourdetail.TourDetailResponse;
import com.example.tour_management.entity.*;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TourDetailService {

    @Autowired
    private TourDetailRepository tourDetailRepository;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<TourDetailResponse> getAll() {
        return tourDetailRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TourDetailResponse getById(Integer id) {
        TourDetail td = tourDetailRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour detail"));

        return mapToResponse(td);
    }

    public List<TourDetailResponse> getByTourId(Integer tourId) {
        return tourDetailRepository.findByTour_Id(tourId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TourDetailResponse> getByHotelId(Integer hotelId) {
        return tourDetailRepository.findByHotel_Id(hotelId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TourDetailResponse> getByVehicleId(Integer vehicleId) {
        return tourDetailRepository.findByVehicle_Id(vehicleId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TourDetailResponse create(TourDetailRequest req) {

        Tour tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        Hotel hotel = hotelRepository.findById(req.getHotelId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hotel"));

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vehicle"));

        TourDetail td = new TourDetail();
        td.setTour(tour);
        td.setHotel(hotel);
        td.setVehicle(vehicle);

        return mapToResponse(tourDetailRepository.save(td));
    }

    public TourDetailResponse update(Integer id, TourDetailRequest req) {

        TourDetail td = tourDetailRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour detail"));

        Tour tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tour"));

        Hotel hotel = hotelRepository.findById(req.getHotelId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hotel"));

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy vehicle"));

        td.setTour(tour);
        td.setHotel(hotel);
        td.setVehicle(vehicle);

        return mapToResponse(tourDetailRepository.save(td));
    }

    public void delete(Integer id) {
        if (!tourDetailRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy tour detail");
        }

        tourDetailRepository.deleteById(id);
    }

    private TourDetailResponse mapToResponse(TourDetail td) {
        TourDetailResponse res = new TourDetailResponse();

        res.setId(td.getId());

        if (td.getTour() != null) {
            res.setTourId(td.getTour().getId());
            res.setTourName(td.getTour().getTourName());
        }

        if (td.getHotel() != null) {
            res.setHotelId(td.getHotel().getId());
            res.setHotelName(td.getHotel().getHotelName());
        }

        if (td.getVehicle() != null) {
            res.setVehicleId(td.getVehicle().getId());
            res.setVehicleName(td.getVehicle().getVehicleName());
        }

        return res;
    }
}