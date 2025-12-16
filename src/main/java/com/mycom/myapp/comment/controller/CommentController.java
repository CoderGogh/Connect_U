package com.mycom.myapp.comment.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.common.PagingResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycom.myapp.auth.details.CustomUserDetails;
import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentResponseDto;
import com.mycom.myapp.comment.dto.CommentTreeResponseDto;
import com.mycom.myapp.comment.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(
		name = "Comment API",
		description = "댓글 / 대댓글 / 정렬 / 페이징 관련 API"
)
public class CommentController {

	private final CommentService commentService;

	// 댓글 + 대댓글 트리 조회 (페이징 + 정렬)
	@GetMapping("/{postId}")
	public ResponseEntity<PagingResultDto<CommentTreeResponseDto>> getCommentsByPost(
	    @PathVariable("postId") Integer postId,

	    @RequestParam(name = "page", defaultValue = "0") int page,
	    @RequestParam(name = "size", defaultValue = "10") int size,
	    @RequestParam(name = "sort", defaultValue = "latest") String sort,

	    @CurrentUsersId(required = false) Integer usersId
	) {

	    Pageable pageable = PageRequest.of(page, size);
	    return ResponseEntity.ok(commentService.getCommentsByPost(postId, usersId, pageable, sort));
	}


	@PostMapping
	@Operation(
	    summary = "댓글 작성",
	    description = "부모 댓글 ID가 없으면 일반 댓글, 있으면 대댓글로 등록됩니다."
	)
	public CommentResponseDto createComment(
	        @RequestBody CommentCreateRequestDto dto,
	        @CurrentUsersId Integer usersId
	) {
	    return commentService.createComment(dto, usersId);
	}


	// 댓글 수정
	@PatchMapping("/{commentId}")
	public CommentResponseDto updateComment(
	    @PathVariable("commentId") Integer commentId,
	    @RequestParam(name = "content") String content,
	    @CurrentUsersId Integer usersId
	) {
	    return commentService.updateComment(commentId, content, usersId);
	}


	// 댓글 삭제 (soft delete)
	@DeleteMapping("/{commentId}")
	@Operation(summary = "댓글 삭제", description = "댓글은 soft delete 처리되며, 작성자 본인만 삭제할 수 있습니다.")
	public void deleteComment(
	    @PathVariable("commentId") Integer commentId,
	    @CurrentUsersId Integer usersId
	) {
	    commentService.deleteComment(commentId, usersId);
	}
}
