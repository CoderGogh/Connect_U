package com.mycom.myapp.follow.entity;

import java.time.LocalDateTime;

import com.mycom.myapp.users.entity.Users;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

    @EmbeddedId
    private FollowKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_src", nullable = false)
    @MapsId("userIdSrc")   // FollowKey.userIdSrc 와 매핑
    private Users userSrc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_dest", nullable = false)
    @MapsId("userIdDest")  // FollowKey.userIdDest 와 매핑
    private Users userDest;

    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Follow(@NonNull Users userSrc, @NonNull Users userDest) {
        this.userSrc = userSrc;
        this.userDest = userDest;

        this.id = new FollowKey(userSrc.getUsersId(), userDest.getUsersId());
        this.createdAt = LocalDateTime.now();
    }

    public static Follow of(@NonNull Users userSrc, @NonNull Users userDest) {
        return new Follow(userSrc, userDest);
    }
}
