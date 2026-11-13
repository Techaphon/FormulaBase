package com.formulabase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @jakarta.persistence.Version
    private long version;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private boolean primaryAdmin = false;
    public boolean isPrimaryAdmin() { return primaryAdmin; }
    public void protectAsPrimaryAdmin() { if (role != UserRole.CEO) throw new IllegalStateException("Only CEO can be primary administrator"); primaryAdmin = true; }

    protected User() {
    }

    public User(String username, String password, String fullName, UserRole role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public boolean isArchived() { return archived; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(UserRole role) { if (primaryAdmin && role != UserRole.CEO) throw new IllegalStateException("Primary administrator is protected"); this.role = role; }
    public void setActive(boolean active) { if (primaryAdmin && !active) throw new IllegalStateException("Primary administrator is protected"); this.active = active; }
    public void archive() { if (primaryAdmin) throw new IllegalStateException("Primary administrator is protected"); this.active = false; this.archived = true; }
}
