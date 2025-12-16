package com.mycom.myapp.post.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostImageDto;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PagingResultDto<PostResponse>> listPosts() {
        PagingResultDto<PostResponse> page = postService.listPosts(null);
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
    public ResponseEntity<PagingResultDto<PostResponse>> getActivePostsLatest(
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "10") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getPostsLatest(startOffset, pageSize));
    }

    @GetMapping("/following-latest")
    public ResponseEntity<PagingResultDto<PostResponse>> getActiveFollowingPostsLatest(
            @CurrentUsersId(required = false) Integer usersId,
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "10") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getFollwingPostLatest(usersId, startOffset, pageSize));
    }

    @GetMapping("/likes")
    public ResponseEntity<PagingResultDto<PostResponse>> getActivePostLikeCountDesc(
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "10") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getPostsLikesDesc(startOffset, pageSize));
    }

    @GetMapping("/following-likes")
    public ResponseEntity<PagingResultDto<PostResponse>> getActiveFollowingPostsLikeCountDesc(
            @CurrentUsersId(required = false) Integer usersId,
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "10") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getFollwingPostLikesDesc(usersId, startOffset, pageSize));
    }

    @PostMapping(path = "/{id}/images", consumes = "multipart/form-data")
    public ResponseEntity<PostImageDto> uploadImage(
            @PathVariable("id") Integer postId,
            @RequestPart("file") MultipartFile file,
            Principal principal
    ) throws Exception {
        PostImageDto dto = postService.uploadPostImage(postId, file, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping(path = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("id") Integer postId,
            @RequestPart("data") CreatePostRequest request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestParam(value = "deleteImageSeqs", required = false) List<Integer> deleteImageSeqs,
            Principal principal
    ) throws Exception {
        PostResponse updated = postService.updatePost(postId, request, deleteImageSeqs, newImages, principal);
        return ResponseEntity.ok(updated);
    }
}
