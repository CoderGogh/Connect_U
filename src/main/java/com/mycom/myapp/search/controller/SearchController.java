package com.mycom.myapp.search.controller;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.post.service.PostService;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Search API",
    description = "키워드 기반 사용자 / 게시글 통합 검색 API"
)
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final UsersService usersService;
    private final PostService postService;

    @GetMapping("/users")
    @Operation(summary = "키워드 기반 회원 닉네임으로 검색")
    public ResponseEntity<PagingResultDto<UsersListResponseDto>> searchUsersByKeyword(@RequestParam("keyword") String keyword, @RequestParam("page") Integer startOffset, @RequestParam("size") Integer pageSize) {
        return ResponseEntity.ok(usersService.getUsersListByNickname(keyword, startOffset, pageSize));
    }
    @GetMapping("/post")
    @Operation(summary = "키워드 기반 게시글 제목/본문을 검색")
    public ResponseEntity<PagingResultDto<PostResponse>> searchPostByKeyword(@RequestParam("keyword") String keyword, @RequestParam("page") Integer startOffset, @RequestParam("size") Integer pageSize) {
        return ResponseEntity.ok(postService.getPostListByKeyword(keyword, startOffset, pageSize));
    }
}
