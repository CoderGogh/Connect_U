package com.mycom.myapp.comment.service;

import java.util.List;

import com.mycom.myapp.common.PagingResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentResponseDto;
import com.mycom.myapp.comment.dto.CommentTreeResponseDto;
import com.mycom.myapp.comment.entity.Comment;

public interface CommentService {

    CommentResponseDto createComment(CommentCreateRequestDto dto, Integer userId);

    PagingResultDto<CommentTreeResponseDto> getCommentsByPost(Integer postId, Integer userId, Pageable pageable , String sort);

    CommentResponseDto updateComment(Integer commentId, String content, Integer userId);
    
    void deleteComment(Integer commentId, Integer userId);
}
