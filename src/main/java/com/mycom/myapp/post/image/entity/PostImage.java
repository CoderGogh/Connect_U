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
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    @ToString.Exclude
    private Post post;


    @Column(nullable = false, unique = true)
    private String imageKey;


    @Column(nullable = false)
    private Long volume;


    @Column(nullable = false, columnDefinition = "tinyint(1)")
    private Boolean isDeleted;


    private LocalDateTime deletedAt;


    @Builder
    public PostImage(@NonNull Post post, @NonNull Integer seq, @NonNull String imageKey, @NonNull Long volume) {
        this.post = post;
        this.id = new PostImageKey(post.getId(), seq);
        this.imageKey = imageKey;
        this.volume = volume;
        this.isDeleted = false;
    }
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

}