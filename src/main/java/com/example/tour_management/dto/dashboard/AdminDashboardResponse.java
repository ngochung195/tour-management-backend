package com.example.tour_management.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalTours;
    private long totalBookings;
    private BigDecimal monthlyRevenue;
    private BigDecimal quarterlyRevenue;
    private long newContacts;
    private List<RevenueChartResponse> revenueChart;
    private List<TopTourResponse> topTours;
    private List<RecentActivityResponse> recentActivities;

    public List<RecentActivityResponse> getRecentActivities() {
        return recentActivities;
    }
    public void setRecentActivities(List<RecentActivityResponse> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalTours() {
        return totalTours;
    }

    public void setTotalTours(long totalTours) {
        this.totalTours = totalTours;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public BigDecimal getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(BigDecimal monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public BigDecimal getQuarterlyRevenue() {
        return quarterlyRevenue;
    }

    public void setQuarterlyRevenue(BigDecimal quarterlyRevenue) {
        this.quarterlyRevenue = quarterlyRevenue;
    }

    public long getNewContacts() {
        return newContacts;
    }

    public void setNewContacts(long newContacts) {
        this.newContacts = newContacts;
    }

    public List<RevenueChartResponse> getRevenueChart() {
        return revenueChart;
    }

    public void setRevenueChart(List<RevenueChartResponse> revenueChart) {
        this.revenueChart = revenueChart;
    }

    public List<TopTourResponse> getTopTours() {
        return topTours;
    }

    public void setTopTours(List<TopTourResponse> topTours) {
        this.topTours = topTours;
    }
}