package com.bookworm.repository;

import com.bookworm.entity.UserLibraryMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLibraryMembershipRepository extends JpaRepository<UserLibraryMembership, Integer> {

    List<UserLibraryMembership> findByUser_UserId(Integer userId);

    List<UserLibraryMembership> findByUser_UserIdAndStatus(Integer userId, com.bookworm.entity.enums.MembershipStatus status);

    List<UserLibraryMembership> findByUser_UserIdAndStatusOrderByExpiryDateAsc(
            Integer userId, com.bookworm.entity.enums.MembershipStatus status);
}
