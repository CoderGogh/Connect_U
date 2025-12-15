package com.mycom.myapp.post.image.entity;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    @ToString.Exclude
    private Post post;

    @Column(name = "image_key", nullable = false, length = 255, unique = true)
    private String imageKey;

    @Column(nullable = false)
    private Long volume;

    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private Boolean isDeleted;

    private LocalDateTime deletedAt;

    @Builder
    public PostImage(@NonNull Post post, @NonNull Integer seq, @NonNull String imageKey, @NonNull Long volume) {
        this.post = post;

        // ERD 기준 --> post_id 와 seq를 복합키로 사용
        this.id = new PostImageKey(
                post.getId(),  // Integer
                seq            // Integer
        );

        // image_key는 엔티티 생성 시점에 일괄적으로 생성.
        // 형식: post/{postId}/{uuid}
        this.imageKey = "post/" + post.getId() + "/" + UUID.randomUUID().toString();
        this.volume = volume;
        this.isDeleted = false;
    }
}
