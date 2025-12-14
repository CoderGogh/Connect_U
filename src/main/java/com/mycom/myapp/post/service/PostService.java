package com.mycom.myapp.post.service;

import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface PostService {
    PostResponse createPost(CreatePostRequest request, Principal principal);
    Page<PostResponse> listPosts(Pageable pageable);
    PostResponse getPost(Integer id);
    void deletePost(Integer id, Principal principal);
}
