package com.example.tour_management.dto.tourdetail;

import jakarta.validation.constraints.NotNull;

public class TourDetailRequest {

    @NotNull
    private Integer tourId;

    @NotNull
    private Integer hotelId;

    @NotNull
    private Integer vehicleId;

    public Integer getTourId() {
        return tourId;
    }

    public void setTourId(Integer tourId) {
        this.tourId = tourId;
    }

    public Integer getHotelId() {
        return hotelId;
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }
}