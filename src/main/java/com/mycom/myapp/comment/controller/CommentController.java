package com.mycom.myapp.comment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
	@Operation(summary = "댓글 트리 조회", description = "부모 댓글 기준으로 페이징 처리되며, 대댓글은 트리 구조로 함께 반환됩니다. "
			+ "기본 정렬은 최신순이며, sort=latest|oldest|like 옵션을 지원합니다.")
	public Page<CommentTreeResponseDto> getCommentsByPost(@PathVariable Integer postId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "latest") String sort, Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Integer userId = userDetails.getId();

		Pageable pageable = PageRequest.of(page, size);
		return commentService.getCommentsByPost(postId, userId, pageable, sort);
	}

	@PostMapping
	@Operation(
	    summary = "댓글 작성",
	    description = "부모 댓글 ID가 없으면 일반 댓글, 있으면 대댓글로 등록됩니다."
	)
	public CommentResponseDto createComment(
	        @RequestBody CommentCreateRequestDto dto,
	        Authentication authentication
	) {
	    Integer userId;

	    if (authentication == null ||
	        !(authentication.getPrincipal() instanceof CustomUserDetails)) {
	        // ⭐ Swagger 테스트용 임시 처리
	        userId = 1; // DB에 존재하는 users_id
	    } else {
	        CustomUserDetails userDetails =
	                (CustomUserDetails) authentication.getPrincipal();
	        userId = userDetails.getId();
	    }

	    return commentService.createComment(dto, userId);
	}


	// 댓글 수정
	@PatchMapping("/{commentId}")
	@Operation(summary = "댓글 수정", description = "댓글 작성자 본인만 수정할 수 있습니다.")
	public CommentResponseDto updateComment(@PathVariable Integer commentId, @RequestParam String content,
			Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Integer userId = userDetails.getId();

		return commentService.updateComment(commentId, content, userId);
	}

	// 댓글 삭제 (soft delete)
	@DeleteMapping("/{commentId}")
	@Operation(summary = "댓글 삭제", description = "댓글은 soft delete 처리되며, 작성자 본인만 삭제할 수 있습니다.")
	public void deleteComment(@PathVariable Integer commentId, Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Integer userId = userDetails.getId();

		commentService.deleteComment(commentId, userId);
	}
}
