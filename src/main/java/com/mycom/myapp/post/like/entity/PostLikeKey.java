package com.mycom.myapp.post.like.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PostLikeKey implements Serializable {

    private Integer usersId;
    private Long postId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostLikeKey)) return false;
        PostLikeKey that = (PostLikeKey) o;
        return Objects.equals(usersId, that.usersId) &&
               Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usersId, postId);
    }
}
