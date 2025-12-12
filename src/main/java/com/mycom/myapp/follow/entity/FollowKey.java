package com.mycom.myapp.follow.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowKey implements Serializable {

    private Integer usersIdSrc;   // 팔로우 하는 사람
    private Integer usersIdDest;  // 팔로우 당하는 사람

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowKey that = (FollowKey) o;
        return Objects.equals(usersIdSrc, that.usersIdSrc) &&
               Objects.equals(usersIdDest, that.usersIdDest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersIdSrc, usersIdDest);
    }
}
