package com.mycom.myapp.users.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UsersRole {
    @EmbeddedId
    private UsersRoleKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    @MapsId("usersId")
    private Users users;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @MapsId("roleId")
    private Role role;
    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    private UsersRole(Users users, Role role) {
        this.id = new UsersRoleKey(users.getUserId(), role.getRoleId());
        this.users = users;
        this.users.getUsersRoles().add(this); // 양방향 매핑
        this.role = role;
        this.isDeleted = false;
    }
    public static UsersRole usersRoleof(@NonNull Users users, @NonNull Role role) {
        return new UsersRole(users, role);
    }
}
