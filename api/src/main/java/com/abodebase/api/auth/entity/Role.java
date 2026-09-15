package com.abodebase.api.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "roles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_roles_name", columnNames = "name")
    }
)
public class Role {

    // ============================================
    // Fields
    // ============================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;


    // ============================================
    // Constructors
    // ============================================

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }


    // ============================================
    // Getters and Setters
    // ============================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}