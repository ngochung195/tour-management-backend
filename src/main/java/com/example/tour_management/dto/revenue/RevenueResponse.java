package com.example.tour_management.dto.revenue;

import java.math.BigDecimal;

public class RevenueResponse {
    private Integer time;
    private BigDecimal revenue;

    public RevenueResponse(Integer time, BigDecimal revenue) {
        this.time = time;
        this.revenue = revenue;
    }

    public Integer getTime() {
        return time;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}
