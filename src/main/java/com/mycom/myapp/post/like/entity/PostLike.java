package com.mycom.myapp.post.like.entity;

import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.users.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike implements Persistable<PostLikeKey> {

    @EmbeddedId
    private PostLikeKey id;  // 기본 빈 객체

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    @MapsId("usersId")   // PostLikeKey.usersId 매핑
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @MapsId("postId")    // PostLikeKey.postId 매핑
    private Post post;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew = true;

    @Builder
    public PostLike(Users users, Post post) {
        this.users = users;
        this.post = post;

        // ERD 기반 PK 전부 int → Integer 통일
        this.id = new PostLikeKey(
                users.getUsersId(),   // Integer
                post.getId()          // Integer
        );
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostLoad
    @PrePersist
    void markNotNew() {
        // 1. 로딩된 엔티티는 '새 엔티티'가 아님을 표기
        // 2. 동일 트랜잭션에서 동일한 post_like 엔티티에 대해 중복 insert 시도를 막기 위해 false 설정
        this.isNew = false;
    }
}
