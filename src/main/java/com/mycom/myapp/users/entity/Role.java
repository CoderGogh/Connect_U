package com.mycom.myapp.users.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private Boolean isDeleted;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Role(String name) {
        this.name = name;
        this.isDeleted = false;
    }
    public static Role of(@NonNull String name) {
        return new Role(name);
    }
}
