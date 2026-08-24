package com.example.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_ID")
    private int userId;

    @Column(name = "User_Name", length = 80, nullable = false)
    private String userName;

    @Column(name = "User_Email", length = 80, nullable = false, unique = true)
    private String userEmail;

    @Column(name = "User_Phone", length = 20)
    private String userPhone;

    @Column(name = "User_Address", columnDefinition = "TEXT")
    private String userAddress;

    /**
     * BCrypt hash. @JsonIgnore because Cart and MyShelf are serialised as
     * entities, and each one carries a User.
     */
    @JsonIgnore
    @Column(name = "User_Password", length = 255, nullable = false)
    private String userPassword;

    @Column(name = "is_Admin", nullable = false)
    private boolean admin;

    @Column(name = "Join_Date")
    private LocalDate joinDate;

    public User() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }
}
