package com.bookworm.security;

import com.bookworm.entity.Admin;
import com.bookworm.entity.User;
import com.bookworm.entity.enums.AdminStatus;
import com.bookworm.entity.enums.UserStatus;
import com.bookworm.repository.AdminRepository;
import com.bookworm.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Admin> admin = adminRepository.findByEmailIgnoreCase(email);
        if (admin.isPresent()) {
            return toUserDetails(admin.get());
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account for email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(user.getStatus() == UserStatus.DISABLED)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name())))
                .build();
    }

    private UserDetails toUserDetails(Admin admin) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(admin.getEmail())
                .password(admin.getPasswordHash())
                .disabled(admin.getStatus() == AdminStatus.DISABLED)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())))
                .build();
    }
}
