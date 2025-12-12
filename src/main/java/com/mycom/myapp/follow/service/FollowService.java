package com.mycom.myapp.follow.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.users.dto.UsersListResponseDto;

import java.util.List;

public interface FollowService {
    /**
     * 팔로우
     * @param usersId 팔로우 수행 주체
     * @param targetUsersId 팔로우할 대상의 유저 식별자
     */
    void follow(Integer usersId, Integer targetUsersId);

    /**
     * 언팔로우
     * @param usersId 언팔로우 수행 주체
     * @param targetUsersId 언팔로우할 대상의 유저 식별자
     */
    void unfollow(Integer usersId, Integer targetUsersId);
    PagingResultDto<UsersListResponseDto> followerList(Integer usersId, Integer startOffset, Integer pageSize); // 자신을 팔로우하는 유저 목록 조회
    PagingResultDto<UsersListResponseDto> followingList(Integer usersId, Integer startOffset, Integer pageSize); // 자신이 팔로잉하는 유저 목록 조회
}
