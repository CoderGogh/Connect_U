package com.mycom.myapp.post.image.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImageKey implements Serializable {

    private Integer postId; // post.post_id
    private Integer seq;    // 이미지 순번 (0 ~ 4)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostImageKey that = (PostImageKey) o;
        return Objects.equals(postId, that.postId)
                && Objects.equals(seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, seq);
    }
}
