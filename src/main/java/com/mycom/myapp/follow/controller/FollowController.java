package com.mycom.myapp.follow.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.follow.service.FollowService;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
@Tag(name = "Follow API", description = "팔로우 관련 API")
public class FollowController {
	private final FollowService followService;

	@PostMapping("/{targetUsersId}")
	@Operation(summary = "팔로우")
	public ResponseEntity<Void> follow(@CurrentUsersId Integer usersId,
			@PathVariable("targetUsersId") Integer targetUsersId) {
		followService.follow(usersId, targetUsersId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{targetUsersId}")
	@Operation(summary = "언팔로우")
	public ResponseEntity<Void> unfollow(@CurrentUsersId Integer usersId,
			@PathVariable("targetUsersId") Integer targetUsersId) {
		followService.unfollow(usersId, targetUsersId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/my-followers")
	@Operation(summary = "자신을 팔로우하는 유저 목록 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요.")
	public ResponseEntity<PagingResultDto<UsersListResponseDto>> getMyFollowers(@CurrentUsersId Integer usersId,
			@RequestParam(value = "page", defaultValue = "0") Integer startOffset,
			@RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
		return ResponseEntity.ok(followService.followerList(usersId, startOffset, pageSize));
	}

	@GetMapping("/my-followings")
	@Operation(summary = "자신이 팔로우하는 유저 목록 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요.")
	public ResponseEntity<PagingResultDto<UsersListResponseDto>> getMyFollowings(@CurrentUsersId Integer usersId,
			@RequestParam(value = "page", defaultValue = "0") Integer startOffset,
			@RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
		return ResponseEntity.ok(followService.followingList(usersId, startOffset, pageSize));
	}

	@GetMapping("/followers/{usersId}")
	@Operation(summary = "특정 유저를 팔로우하는 유저 목록 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요.")
	public ResponseEntity<PagingResultDto<UsersListResponseDto>> getUsersFollowers(
			@PathVariable("usersId") Integer usersId,
			@RequestParam(value = "page", defaultValue = "0") Integer startOffset,
			@RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
		return ResponseEntity.ok(followService.followerList(usersId, startOffset, pageSize));
	}

	@GetMapping("/followings/{usersId}")
	@Operation(summary = "특정 유저가 팔로우하는 유저 목록 조회", description = "페이지 번호(page), 페이지 크기(size)를 URL 쿼리 파라미터로 전달해주세요.")
	public ResponseEntity<PagingResultDto<UsersListResponseDto>> getUsersFollowings(
			@PathVariable("usersId") Integer usersId,
			@RequestParam(value = "page", defaultValue = "0") Integer startOffset,
			@RequestParam(value = "size", defaultValue = "1") Integer pageSize) {
		return ResponseEntity.ok(followService.followingList(usersId, startOffset, pageSize));
	}

}
