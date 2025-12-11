package com.mycom.myapp.comment.like.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentLikeResponseDto {
    private Integer commentId;
    private Integer likeCount;
    private Boolean isLiked; // true = 좋아요 O, false = 좋아요 취소됨
}
