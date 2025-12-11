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
    @MapsId("postId")   // PostImageKey.postId 매핑
    @ToString.Exclude
    private Post post;

    @Column(name = "image_key", nullable = false, length = 255)
    private String imageKey;

    @Column(nullable = false)
    private Long volume;

    @Column(nullable = false)
    private Boolean isDeleted;

    private LocalDateTime deletedAt;

    @Builder
    public PostImage(Post post, Integer seq, String imageKey, Long volume) {
        this.post = post;

        // ERD 기준 PK = int → Integer 기반
        this.id = new PostImageKey(
                post.getId(),  // Integer
                seq            // Integer
        );

        this.imageKey = imageKey;
        this.volume = volume;
        this.isDeleted = false;
    }
}
