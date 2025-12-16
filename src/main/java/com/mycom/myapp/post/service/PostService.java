package com.mycom.myapp.post.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostImageDto;
import com.mycom.myapp.post.dto.PostResponse;
import org.springframework.data.domain.Pageable;
import com.mycom.myapp.common.PagingResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface PostService {

    /**
     * 게시글 생성
     */
    PostResponse createPost(CreatePostRequest request, Integer usersId);

    /**
     * 게시글 목록 조회 (페이징)
     */
    PagingResultDto<PostResponse> listPosts(Pageable pageable);

    /**
     * 게시글 단건 조회
     */
    PostResponse getPost(Integer id);

    /**
     * 게시글 삭제 (soft delete)
     */
    void deletePost(Integer id, Principal principal);

    /**
     * 게시글 이미지 업로드
     */
    PostImageDto uploadPostImage(
            Integer postId,
            MultipartFile file,
            Principal principal
    ) throws Exception;

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
    PagingResultDto<PostResponse> getPostListByKeyword(String keyword, Integer startOffset, Integer pageSize);
}
