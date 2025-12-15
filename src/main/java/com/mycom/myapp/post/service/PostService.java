package com.mycom.myapp.post.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface PostService {
    PostResponse createPost(CreatePostRequest request, Integer usersId);
    Page<PostResponse> listPosts(Pageable pageable);
    PostResponse getPost(Integer id);
    void deletePost(Integer id, Principal principal);

    /**
     * 게시글 최신순 조회
     * @param startOffset 조회 시작 페이지 번호
     * @param pageSize 페이지 크기
     * @return
     */
    PagingResultDto<PostResponse> getPostsLatest(Integer startOffset, Integer pageSize);
    /**
     * 팔로우 대상이 작성한 게시글 최신순 조회
     * @param usersId 인증 정보의 유저 식별자 값(null 허용)
     * @param startOffset 조회 시작 페이지 번호
     * @param pageSize 페이지 크기
     * @return
     */
    PagingResultDto<PostResponse> getFollwingPostLatest(Integer usersId, Integer startOffset, Integer pageSize);

    /**
     * 게시글 좋아요순 조회
     * @param startOffset 조회 시작 페이지 번호
     * @param pageSize 페이지 크기
     * @return
     */
    PagingResultDto<PostResponse> getPostsLikesDesc(Integer startOffset, Integer pageSize);
    /**
     * 팔로우 대상이 작성한 게시글 좋아요순 조회
     * @param usersId 인증 정보의 유저 식별자 값(null 허용)
     * @param startOffset 조회 시작 페이지 번호
     * @param pageSize 페이지 크기
     * @return
     */
    PagingResultDto<PostResponse> getFollwingPostLikesDesc(Integer usersId, Integer startOffset, Integer pageSize);
}
