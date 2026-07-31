package com.example.tour_management.dto.promotion;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromotionRequest {

    @NotBlank(message = "Code không được để trống")
    private String code;

    @NotNull(message = "Discount không được null")
    @DecimalMin(value = "0.01", message = "Discount phải > 0")
    @DecimalMax(value = "100.00", message = "Discount <= 100")
    private BigDecimal discount;

    @NotNull(message = "Start date không được null")
    private LocalDateTime startDate;

    @NotNull(message = "End date không được null")
    private LocalDateTime endDate;

    @JsonProperty("isActive")
    private Boolean active;

    public PromotionRequest() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}