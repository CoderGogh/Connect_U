package com.mycom.myapp.comment.like.entity;

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
public class CommentLikeKey implements Serializable {

    private Long commentId;
    private Integer usersId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentLikeKey)) return false;
        CommentLikeKey that = (CommentLikeKey) o;
        return Objects.equals(commentId, that.commentId)
                && Objects.equals(usersId, that.usersId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, usersId);
    }
}
