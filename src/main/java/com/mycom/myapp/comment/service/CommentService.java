package com.mycom.myapp.comment.service;

import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {

    CommentResponseDto createComment(CommentCreateRequestDto dto, Integer userId);

    List<CommentResponseDto> getCommentsByPost(Integer postId, Integer userId);

    CommentResponseDto updateComment(Integer commentId, String content, Integer userId);

    void deleteComment(Integer commentId, Integer userId);
}
