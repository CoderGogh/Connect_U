package com.mycom.myapp.post.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request, @CurrentUsersId Integer usersId) {
        PostResponse created = postService.createPost(request, usersId);
        return ResponseEntity.created(URI.create("/api/posts/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> listPosts(Pageable pageable) {
        Page<PostResponse> page = postService.listPosts(pageable);
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
}
