package com.mycom.myapp.post.image.entity;

import java.time.LocalDateTime;

import com.mycom.myapp.post.entity.Post;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_image")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {

    @EmbeddedId
    private PostImageKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @MapsId("postId") // Integer 기반으로 매핑됨
    @ToString.Exclude
    private Post post;

    @Column(name = "image_key", nullable = false, length = 255)
    private String imageKey;

    @Column(name = "volume", nullable = false)
    private Long volume;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public PostImage(Post post, Integer seq, String imageKey, Long volume) {
        this.post = post;
        this.id = new PostImageKey(post.getId(), seq); // 변경됨
        this.imageKey = imageKey;
        this.volume = volume;
        this.isDeleted = false;
    }
}
