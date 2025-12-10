package com.mycom.myapp.post.image.entity;

import java.time.LocalDateTime;

import com.mycom.myapp.post.entity.Post;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @MapsId("postId") // PostImageKey.postId 와 매핑
    @ToString.Exclude
    private Post post;

    @Column(name = "key", nullable = false, length = 255)
    private String key;

    @Column(name = "volume", nullable = false)
    private Long volume;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public PostImage(Post post, Integer seq, String key, Long volume) {
        this.post = post;
        this.id = new PostImageKey(post.getId().intValue(), seq);
        this.key = key;
        this.volume = volume;
        this.isDeleted = false;
    }
}
