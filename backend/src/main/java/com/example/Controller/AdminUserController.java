package com.example.Controller;

import com.example.Services.AdminUserService;
import com.example.dto.AdminUserSummaryDto;
import com.example.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final CurrentUser currentUser;

    public AdminUserController(AdminUserService adminUserService, CurrentUser currentUser) {
        this.adminUserService = adminUserService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<AdminUserSummaryDto> getAllUsers() {
        return adminUserService.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        adminUserService.deleteUser(id, currentUser.require());
        return ResponseEntity.noContent().build();
    }
}
