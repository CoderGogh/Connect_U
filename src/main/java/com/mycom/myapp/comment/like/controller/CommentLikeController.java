package com.mycom.myapp.comment.like.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.auth.details.CustomUserDetails;
import com.mycom.myapp.comment.like.dto.CommentLikeResponseDto;
import com.mycom.myapp.comment.like.service.CommentLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments/likes")
@RequiredArgsConstructor
@Tag(name = "Comment Like API", description = "댓글 좋아요 관련 API")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

 
    @Operation(
        summary = "댓글 좋아요 토글",
        description = "해당 댓글에 좋아요를 누르거나, 이미 눌렀다면 좋아요를 취소합니다."
    )
    @PostMapping("/{commentId}")
    public CommentLikeResponseDto toggleLike(
            @PathVariable("commentId") Integer commentId,
            @CurrentUsersId Integer usersId
    ) {
        return commentLikeService.toggleLike(commentId, usersId);
    }

}

