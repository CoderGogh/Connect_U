package com.mycom.myapp.post.like.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.post.like.service.PostLikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/likes")
@Tag(name = "게시글 좋아요 API")
public class PostLikeController {
    private final PostLikeService postLikeService;

    // 좋아요 추가
    @PostMapping("/{postId}")
    public ResponseEntity<Void> createLike(@PathVariable Integer postId, @CurrentUsersId Integer usersId) {
        postLikeService.createLike(usersId, postId);
        return ResponseEntity.ok().build();
    }

    // 좋아요 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteLike(@PathVariable Integer postId, @CurrentUsersId Integer usersId) {
        postLikeService.deleteLike(usersId, postId);
        return ResponseEntity.ok().build();
    }
}
