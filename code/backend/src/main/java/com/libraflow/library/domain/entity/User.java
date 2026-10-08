package com.libraflow.library.domain.entity;

import com.libraflow.library.domain.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 50
    )
    private String username;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 100
    )
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private UserRole role;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "member_tier", length = 20)
    private String memberTier = "STUDENT";

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    @PrimaryKeyJoinColumn
    private UserProfile profile;

    @Column(
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected User() {
        // JPA only
    }

    // ==========================================
    // เพิ่ม Constructor สำหรับใช้ตอนสมัครสมาชิก
    // ==========================================
    public User(String username, String passwordHash, String email, UserRole role, boolean active, String memberTier) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
        this.active = active;
        this.memberTier = memberTier;
    }

    // Getters เดิม
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getMemberTier() {
        return memberTier;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters เดิม
    public void setMemberTier(String memberTier) {
        this.memberTier = memberTier;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    // ==========================================
    // เพิ่ม Setters เฉพาะที่จำเป็นให้แอดมินใช้งาน
    // ==========================================
    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}