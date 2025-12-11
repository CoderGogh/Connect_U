package com.mycom.myapp.comment.service;

import java.util.List;

import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentResponseDto;
import com.mycom.myapp.comment.dto.CommentTreeResponseDto;

public interface CommentService {

    CommentResponseDto createComment(CommentCreateRequestDto dto, Integer userId);

    List<CommentTreeResponseDto> getCommentsByPost(Integer postId, Integer userId)

    CommentResponseDto updateComment(Integer commentId, String content, Integer userId);

    void deleteComment(Integer commentId, Integer userId);
}
