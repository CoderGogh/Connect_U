package com.mycom.myapp.post.like.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.post.like.service.PostLikeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/likes")
@Tag(
    name = "Posts",
    description = "게시글(Post) 좋아요 추가 및 취소 API"
)
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}")
    @Operation(
        summary = "게시글 좋아요 추가",
        description = "로그인한 사용자가 특정 게시글에 좋아요를 추가합니다."
    )
    public ResponseEntity<Void> createLike(
            @PathVariable Integer postId,
            @CurrentUsersId Integer usersId
    ) {
        postLikeService.createLike(usersId, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    @Operation(
        summary = "게시글 좋아요 취소",
        description = "로그인한 사용자가 특정 게시글의 좋아요를 취소합니다."
    )
    public ResponseEntity<Void> deleteLike(
            @PathVariable Integer postId,
            @CurrentUsersId Integer usersId
    ) {
        postLikeService.deleteLike(usersId, postId);
        return ResponseEntity.ok().build();
    }
}
