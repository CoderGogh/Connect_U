package com.mycom.myapp.follow.service;

import com.mycom.myapp.follow.dto.FollowRequestDto;
import com.mycom.myapp.users.dto.UsersListResponseDto;

import java.util.List;

public interface FollowService {
    void follow(FollowRequestDto dto);
    void unfollow(FollowRequestDto dto);
    List<UsersListResponseDto> followerList(Integer usersId); // 자신을 팔로우하는 유저 목록 조회
    List<UsersListResponseDto> followingList(Integer usersId); // 자신이 팔로잉하는 유저 목록 조회
}
