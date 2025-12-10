package com.mycom.myapp.users.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable // 복합 PK 클래스
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UsersRoleKey implements Serializable {
    private Integer usersId;
    private Integer roleId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UsersRoleKey that = (UsersRoleKey) o;
        return Objects.equals(usersId, that.usersId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersId, roleId);
    }
}
