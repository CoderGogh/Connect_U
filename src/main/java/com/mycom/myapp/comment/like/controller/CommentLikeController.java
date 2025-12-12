package com.mycom.myapp.comment.like.controller;

import com.mycom.myapp.auth.details.CustomUserDetails;
import com.mycom.myapp.comment.like.dto.CommentLikeResponseDto;
import com.mycom.myapp.comment.like.service.CommentLikeService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments/likes")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/{commentId}")
    public CommentLikeResponseDto toggleLike(
            @PathVariable Integer commentId,
            Authentication authentication
    ) {
        // SecurityContext에 저장된 사용자 정보 꺼내기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 사용자 PK (usersId)
        Integer userId = userDetails.getId();

        return commentLikeService.toggleLike(commentId, userId);
    }
}
