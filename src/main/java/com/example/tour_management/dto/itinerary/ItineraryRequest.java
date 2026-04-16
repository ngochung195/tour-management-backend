package com.example.tour_management.dto.itinerary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public class ItineraryRequest {
    private Integer id;

    @NotNull(message = "tourId không được null")
    private Integer tourId;

    @NotNull(message = "dayNumber không được null")
    @Min(value = 1, message = "dayNumber phải >= 1")
    private Integer dayNumber;

    @NotNull(message = "time không được null")
    private LocalTime time;

    @NotBlank(message = "activity không được rỗng")
    private String activity;

    private String description;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTourId() {
        return tourId;
    }

    public void setTourId(Integer tourId) {
        this.tourId = tourId;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}