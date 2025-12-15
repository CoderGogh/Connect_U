package com.mycom.myapp.post.service;

import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostImageDto;
import com.mycom.myapp.post.dto.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface PostService {

    /**
     * 게시글 생성
     * - 인증된 사용자(Principal) 기준으로 작성자 식별
     */
    PostResponse createPost(CreatePostRequest request, Principal principal);

    /**
     * 게시글 목록 조회 (페이징)
     */
    Page<PostResponse> listPosts(Pageable pageable);

    /**
     * 게시글 단건 조회
     */
    PostResponse getPost(Integer id);

    /**
     * 게시글 삭제 (soft delete)
     * - 작성자 본인만 가능
     */
    void deletePost(Integer id, Principal principal);

    /**
     * 게시글 이미지 업로드 (2-step API)
     * - 게시글 생성 후 이미지 개별 업로드
     */
    PostImageDto uploadPostImage(
            Integer postId,
            MultipartFile file,
            Principal principal
    ) throws Exception;
}
