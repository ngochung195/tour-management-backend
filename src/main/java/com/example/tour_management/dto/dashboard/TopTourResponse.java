package com.example.tour_management.dto.dashboard;

import java.math.BigDecimal;

public class TopTourResponse {

    private Integer tourId;
    private String tourName;
    private Long totalBookings;
    private BigDecimal revenue;
    private String location;
    private String status;
    private Integer daysUntilStart;

    public TopTourResponse(Integer tourId, String tourName, Long totalBookings, BigDecimal revenue) {
        this.tourId = tourId;
        this.tourName = tourName;
        this.totalBookings = totalBookings;
        this.revenue = revenue;
    }

    public Integer getTourId() {
        return tourId;
    }

    public String getTourName() {
        return tourName;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public String getLocation()           { return location; }

    public void setLocation(String v)     { this.location = v; }

    public String getStatus()             { return status; }

    public void setStatus(String v)       { this.status = v; }

    public Integer getDaysUntilStart()    { return daysUntilStart; }

    public void setDaysUntilStart(Integer v) { this.daysUntilStart = v; }

}