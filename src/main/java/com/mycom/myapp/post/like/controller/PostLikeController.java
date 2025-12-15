package com.mycom.myapp.post.like.controller;

import com.mycom.myapp.post.like.service.PostLikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/likes")
@Tag(name = "게시글 좋아요 API")
public class PostLikeController {
    private final PostLikeService postLikeService;

    // 좋아요 추가

    // 좋아요 삭제
}
