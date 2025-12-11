package com.mycom.myapp.comment.like.entity;

import java.time.LocalDateTime;

import com.mycom.myapp.comment.entity.Comment;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CommentLike {

    @EmbeddedId
    private CommentLikeKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    @MapsId("commentId")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    @MapsId("usersId")
    private Users users;

    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private Boolean isDeleted;

    private LocalDateTime deletedAt;

    private CommentLike(Comment comment, Users users) {

        // ★ ERD 기준 모든 PK 타입 = int → Integer로 통일
        this.id = new CommentLikeKey(
                comment.getId(),          // Integer
                users.getUsersId()        // Integer (수정 포인트!)
        );

        this.comment = comment;
        this.users = users;
        this.isDeleted = false;
    }

    public static CommentLike of(@NonNull Comment comment, @NonNull Users users) {
        return new CommentLike(comment, users);
    }
}
