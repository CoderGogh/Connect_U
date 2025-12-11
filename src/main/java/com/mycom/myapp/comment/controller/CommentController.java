package com.mycom.myapp.comment.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.auth.details.CustomUserDetails;
import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentResponseDto;
import com.mycom.myapp.comment.dto.CommentTreeResponseDto;
import com.mycom.myapp.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    
    // 댓글 + 대댓글 트리 조회
    @GetMapping("/post/{postId}")
    public List<CommentTreeResponseDto> getCommentsByPost(
            @PathVariable Integer postId,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getId();

        return commentService.getCommentsByPost(postId, userId);
    }

    
    
    @PostMapping
    public CommentResponseDto createComment(
            @RequestBody CommentCreateRequestDto dto,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getId();

        return commentService.createComment(dto, userId);
    }

    @PatchMapping("/{commentId}")
    public CommentResponseDto updateComment(
            @PathVariable Integer commentId,
            @RequestParam String content,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getId();

        return commentService.updateComment(commentId, content, userId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @PathVariable Integer commentId,
            Authentication authentication
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getId();

        commentService.deleteComment(commentId, userId);
    }
    
    
   
}

