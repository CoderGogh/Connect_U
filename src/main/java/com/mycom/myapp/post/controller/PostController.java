package com.mycom.myapp.post.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestBody CreatePostRequest request,
            @CurrentUsersId Integer usersId
    ) {
        PostResponse created = postService.createPost(request, usersId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }


    @GetMapping
    public ResponseEntity<PagingResultDto<PostResponse>> listPosts(Pageable pageable) {
        PagingResultDto<PostResponse> page = postService.listPosts(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Integer id) {
        PostResponse dto = postService.getPost(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id, Principal principal) {
        postService.deletePost(id, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    @Operation(summary = "게시글 전체 최신순 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요. 이 API 반환값의 content가 빈 리스트면 직전 조회 결과값이 마지막 페이지였음을 의미합니다.")
    public ResponseEntity<PagingResultDto<PostResponse>> getActivePostsLatest(@RequestParam(value = "page", defaultValue = "0") Integer startOffset, @RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
        return ResponseEntity.ok(postService.getPostsLatest(startOffset, pageSize));
    }

    @GetMapping("/following-latest")
    @Operation(summary = "팔로우 대상이 작성한 게시글 최신순 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요. 이 API 반환값의 content가 빈 리스트면 그때부터는 '게시글 전체 최신순 조회 API'를 0 페이지부터 요청해주세요.")
    public ResponseEntity<PagingResultDto<PostResponse>> getActiveFollowingPostsLatest(@CurrentUsersId(required = false) Integer usersId, @RequestParam(value = "page", defaultValue = "0") Integer startOffset, @RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
        return ResponseEntity.ok(postService.getFollwingPostLatest(usersId, startOffset, pageSize));
    }

    @GetMapping("/likes")
    @Operation(summary = "게시글 전체 좋아요순 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요. 이 API 반환값의 content가 빈 리스트면 직전 조회 결과값이 마지막 페이지였음을 의미합니다.")
    public ResponseEntity<PagingResultDto<PostResponse>> getActivePostLikeCountDesc(@RequestParam(value = "page", defaultValue = "0") Integer startOffset, @RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
        return ResponseEntity.ok(postService.getPostsLikesDesc(startOffset, pageSize));
    }

    @GetMapping("/following-likes")
    @Operation(summary = "팔로우 대상이 작성한 게시글 좋아요순 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요. 이 API 반환값의 content가 빈 리스트면 그때부터는 '게시글 전체 좋아요순 조회 API'를 0 페이지부터 요청해주세요.")
    public ResponseEntity<PagingResultDto<PostResponse>> getActiveFollowingPostsLikeCountDesc(@CurrentUsersId(required = false) Integer usersId, @RequestParam(value = "page", defaultValue = "0") Integer startOffset, @RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
        return ResponseEntity.ok(postService.getFollwingPostLikesDesc(usersId, startOffset, pageSize));
    }

    @PostMapping(path = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<com.mycom.myapp.post.dto.PostImageDto> uploadImage(@PathVariable("id") Integer postId,
                                                                            @RequestPart("file") MultipartFile file,
                                                                            Principal principal) throws Exception {
        com.mycom.myapp.post.dto.PostImageDto dto = postService.uploadPostImage(postId, file, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
