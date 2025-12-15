package com.mycom.myapp.comment.entity;

import java.time.LocalDateTime;

import com.mycom.myapp.post.entity.PostEntity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mycom.myapp.users.entity.Users;

import jakarta.persistence.*;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer id;

    // 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity postEntity;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private Users users;

    // 부모 댓글 (대댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    // 내용
    @Column(nullable = true, columnDefinition = "text")
    private String content;

    // 대댓글 개수
    @Column(name = "child_count", nullable = false)
    private Integer childCount = 0;

    // 좋아요 수
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Comment(PostEntity postEntity, Users users, Comment parentComment, String content) {
        this.postEntity = postEntity;
        this.users = users;
        this.parentComment = parentComment;
        this.content = content;
    }

    // 비즈니스 로직
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseChildCount() { this.childCount++; }

    public void decreaseChildCount() { this.childCount--; }

    public void increaseLikeCount() { this.likeCount++; }

    public void decreaseLikeCount() { this.likeCount--; }

    public void updateContent(String content) {
        this.content = content;
    }
}

