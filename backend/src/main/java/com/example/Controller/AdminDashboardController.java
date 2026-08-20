package com.example.Controller;

import com.example.Services.AdminDashboardService;
import com.example.dto.AdminDashboardDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The admin home page's headline numbers. Admin only - see SecurityConfig. */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public AdminDashboardDto getDashboard() {
        return adminDashboardService.getDashboard();
    }
}
