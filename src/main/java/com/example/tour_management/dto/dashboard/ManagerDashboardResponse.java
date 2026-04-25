package com.example.tour_management.dto.dashboard;

import com.example.tour_management.dto.booking.BookingResponse;
import com.example.tour_management.dto.review.ReviewResponse;

import java.math.BigDecimal;
import java.util.List;

public class ManagerDashboardResponse {

    private BigDecimal totalRevenue;
    private long pendingBookings;
    private long activeTours;
    private Double avgRating;

    private List<RevenueChartResponse> revenueChart;
    private List<BookingResponse> recentBookings;
    private List<ReviewResponse> recentReviews;
    private List<TopTourResponse> topTours;

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(long pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public long getActiveTours() {
        return activeTours;
    }

    public void setActiveTours(long activeTours) {
        this.activeTours = activeTours;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public List<RevenueChartResponse> getRevenueChart() {
        return revenueChart;
    }

    public void setRevenueChart(List<RevenueChartResponse> revenueChart) {
        this.revenueChart = revenueChart;
    }

    public List<BookingResponse> getRecentBookings() {
        return recentBookings;
    }

    public void setRecentBookings(List<BookingResponse> recentBookings) {
        this.recentBookings = recentBookings;
    }

    public List<ReviewResponse> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<ReviewResponse> recentReviews) {
        this.recentReviews = recentReviews;
    }


    public List<TopTourResponse> getTopTours() { return topTours; }
    public void setTopTours(List<TopTourResponse> topTours) { this.topTours = topTours; }
}