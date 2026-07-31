package com.bookworm.service;

import com.bookworm.dto.auth.AuthResponse;
import com.bookworm.dto.auth.LoginRequest;
import com.bookworm.dto.auth.RegisterRequest;
import com.bookworm.dto.auth.UserSummary;
import com.bookworm.entity.Admin;
import com.bookworm.entity.User;
import com.bookworm.entity.enums.AdminStatus;
import com.bookworm.entity.enums.UserStatus;
import com.bookworm.entity.enums.UserType;
import com.bookworm.exception.EmailAlreadyExistsException;
import com.bookworm.exception.InvalidCredentialsException;
import com.bookworm.repository.AdminRepository;
import com.bookworm.repository.UserRepository;
import com.bookworm.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserSummary register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .occupation(request.getOccupation())
                .professionalDomain(request.getProfessionalDomain())
                .userType(UserType.MEMBER)
                // Phase 1 simplification: accounts are auto-activated (no email/SMTP wired up yet).
                // Revisit alongside the activation_token/email flow in a later phase if needed.
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        return UserSummary.from(saved);
    }

    public AuthResponse login(LoginRequest request) {
        Optional<Admin> admin = adminRepository.findByEmailIgnoreCase(request.getEmail());
        if (admin.isPresent()) {
            return loginAdmin(admin.get(), request.getPassword());
        }

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, UserSummary.from(user));
    }

    private AuthResponse loginAdmin(Admin admin, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, admin.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (admin.getStatus() == AdminStatus.DISABLED) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(admin.getEmail());
        return new AuthResponse(token, UserSummary.from(admin));
    }
}
