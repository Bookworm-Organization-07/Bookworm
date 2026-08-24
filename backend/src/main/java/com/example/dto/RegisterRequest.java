package com.example.dto;

/**
 * Registration payload. There is deliberately no "admin" field: this
 * endpoint is public, so accepting an admin flag from the request body
 * would let anyone grant themselves admin. Admins are seeded in the
 * database.
 */
public class RegisterRequest {

    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
