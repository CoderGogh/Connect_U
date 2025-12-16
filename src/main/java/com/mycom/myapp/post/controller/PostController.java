package com.mycom.myapp.post.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostImageDto;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(
    name = "Posts",
    description = "게시글(Post) 생성, 조회, 삭제 및 이미지 업로드 API"
)
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(
        summary = "게시글 작성",
        description = "로그인한 사용자가 새로운 게시글을 작성합니다."
    )
    public ResponseEntity<PostResponse> createPost(
            @RequestBody CreatePostRequest request,
            @CurrentUsersId Integer usersId
    ) {
        PostResponse created = postService.createPost(request, usersId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(
        summary = "게시글 목록 조회 (기본)",
        description = "전체 게시글을 페이징하여 조회합니다. Pageable(page, size, sort)를 지원합니다."
    )
    public ResponseEntity<PagingResultDto<PostResponse>> listPosts(Pageable pageable) {
        PagingResultDto<PostResponse> page = postService.listPosts(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "게시글 단건 조회",
        description = "게시글 ID를 기준으로 게시글 상세 정보를 조회합니다."
    )
    public ResponseEntity<PostResponse> getPost(@PathVariable Integer id) {
        PostResponse dto = postService.getPost(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "게시글 삭제",
        description = "게시글 작성자 본인만 게시글을 삭제할 수 있습니다. (Soft Delete)"
    )
    public ResponseEntity<Void> deletePost(@PathVariable Integer id, Principal principal) {
        postService.deletePost(id, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    @Operation(
        summary = "게시글 전체 최신순 조회",
        description = """
            게시글을 최신순으로 조회합니다.
            page, size를 쿼리 파라미터로 전달하세요.
            반환된 content가 빈 리스트라면 마지막 페이지입니다.
            """
    )
    public ResponseEntity<PagingResultDto<PostResponse>> getActivePostsLatest(
            @CurrentUsersId(required = false) Integer usersId,
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "1") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getPostsLatest(usersId, startOffset, pageSize));
    }

    @GetMapping("/following-latest")
    @Operation(
        summary = "팔로우한 사용자의 게시글 최신순 조회",
        description = """
            로그인 사용자가 팔로우한 대상의 게시글을 최신순으로 조회합니다.
            조회 결과가 더 이상 없으면 게시글 전체 최신순 조회 API를 사용하세요.
            """
    )
    public ResponseEntity<PagingResultDto<PostResponse>> getActiveFollowingPostsLatest(
            @CurrentUsersId(required = false) Integer usersId,
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "1") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getFollwingPostLatest(usersId, startOffset, pageSize));
    }

    @GetMapping("/likes")
    @Operation(
        summary = "게시글 전체 좋아요순 조회",
        description = "게시글을 좋아요 개수 기준 내림차순으로 조회합니다."
    )
    public ResponseEntity<PagingResultDto<PostResponse>> getActivePostLikeCountDesc(
            @CurrentUsersId(required = false) Integer usersId,
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "1") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getPostsLikesDesc(usersId, startOffset, pageSize));
    }

    @GetMapping("/following-likes")
    @Operation(
        summary = "팔로우한 사용자의 게시글 좋아요순 조회",
        description = "팔로우 대상이 작성한 게시글을 좋아요 수 기준으로 조회합니다."
    )
    public ResponseEntity<PagingResultDto<PostResponse>> getActiveFollowingPostsLikeCountDesc(
            @CurrentUsersId(required = false) Integer usersId,
            @RequestParam(value = "page", defaultValue = "0") Integer startOffset,
            @RequestParam(value = "size", defaultValue = "1") Integer pageSize
    ) {
        return ResponseEntity.ok(postService.getFollwingPostLikesDesc(usersId, startOffset, pageSize));
    }

    @PostMapping(path = "/{id}/images", consumes = "multipart/form-data")
    @Operation(
        summary = "게시글 이미지 업로드",
        description = """
            게시글에 이미지를 업로드합니다.
            multipart/form-data 형식으로 'files' key로 파일을 전달해야 합니다.
            여러 파일을 한 번에 업로드할 수 있으며, 선택한 순서대로 seq가 할당됩니다.
            
            Postman 사용법:
            - Body 탭에서 form-data 선택
            - Key: 'files' (Type: File)
            - Value: 파일 선택 창에서 여러 파일을 한 번에 선택 (Ctrl+클릭 또는 Cmd+클릭)
            """
    )
    public ResponseEntity<List<PostImageDto>> uploadImages(
            @PathVariable("id") Integer postId,
            @RequestPart("files") List<MultipartFile> files,
            Principal principal
    ) throws Exception {
        List<PostImageDto> dtos = postService.uploadPostImages(postId, files, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("id") Integer postId,
            @RequestBody CreatePostRequest request,
            @RequestParam(value = "deleteImageSeqs", required = false) List<Integer> deleteImageSeqs,
            Principal principal
    ) throws Exception {
        // 텍스트 데이터는 JSON(@RequestBody)으로만 수정하고,
        // 새 이미지 업로드는 별도의 /{id}/images multipart 엔드포인트를 사용하도록 통일
        PostResponse updated = postService.updatePost(postId, request, deleteImageSeqs, null, principal);
        return ResponseEntity.ok(updated);
    }
}
