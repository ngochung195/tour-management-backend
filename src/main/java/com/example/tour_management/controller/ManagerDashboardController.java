package com.example.tour_management.controller;

import com.example.tour_management.dto.dashboard.ManagerDashboardResponse;
import com.example.tour_management.service.ManagerDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/dashboard")
public class ManagerDashboardController {

    private final ManagerDashboardService service;

    public ManagerDashboardController(ManagerDashboardService service) {
        this.service = service;
    }

    @GetMapping
    public ManagerDashboardResponse getDashboard(Authentication authentication) {
        return service.getDashboard(authentication);
    }
}