package com.mycom.myapp.users.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Users {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String nickname;
    @Column(columnDefinition = "text")
    private String description;
    @Column(unique = true)
    private String imageKey;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    @Builder
    public Users(@NonNull String email, @NonNull String password, @NonNull String nickname, String description, String imageKey) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.description = description;
        this.imageKey = imageKey;
        this.isDeleted = false;
    }
}
