package com.example.tour_management.dto.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromotionResponse {

    private Integer id;
    private String code;
    private BigDecimal discount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;

    public PromotionResponse() {}

    // ===== GETTER =====
    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}