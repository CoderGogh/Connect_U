package com.mycom.myapp.comment.like.service;

import com.mycom.myapp.comment.like.dto.CommentLikeResponseDto;

public interface CommentLikeService {
    CommentLikeResponseDto toggleLike(Integer commentId, Integer userId);
}
