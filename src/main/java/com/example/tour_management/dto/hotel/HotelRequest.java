package com.example.tour_management.dto.hotel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HotelRequest {

    @NotBlank(message = "Hotel name is required")
    @Size(max = 100)
    private String hotelName;

    @Size(max = 100)
    private String description;

    @NotBlank(message = "Address is required")
    @Size(max = 100)
    private String address;

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}