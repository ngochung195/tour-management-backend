package com.example.tour_management.dto.dashboard;

import java.math.BigDecimal;

public class RevenueChartResponse {

    private Integer month;
    private BigDecimal total;

    public RevenueChartResponse(Integer label, BigDecimal total) {
        this.month = label;
        this.total = total;
    }

    public Integer getMonth() {
        return month;
    }

    public BigDecimal getTotal() {
        return total;
    }
}