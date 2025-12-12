package com.mycom.myapp.follow.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.follow.entity.Follow;
import com.mycom.myapp.follow.repository.FollowRepository;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import com.mycom.myapp.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final int FOLLOW_MAX_PAGE_SIZE = 100;
    private final FollowRepository followRepository;
    private final UsersRepository usersRepository;
    private final UsersService usersService;

    /**
     * 팔로우 리스트 조회 시 페이지 크기 범위 검증
     * @param pageSize 프론트로부터 전달받은 페이지 사이즈
     */
    private Integer verifyFollowPageSize(Integer pageSize) {
        if(pageSize < 1)
            return 1;
        if(pageSize > FOLLOW_MAX_PAGE_SIZE)
            return FOLLOW_MAX_PAGE_SIZE;
        return pageSize;
    }

    /**
     * 팔로우 리스트 조회 시 페이지 번호 범위 검증
     * @param startOffset 프론트로부터 전달받은 페이지 번호
     * @return
     */
    private Integer verifyFollowStartOffset(Integer startOffset) {
        if(startOffset < 0)
            return 0;
        return startOffset;
    }

    @Override
    public void follow(Integer usersId, Integer targetUsersId) {
        if(usersId.equals(targetUsersId)) {
            throw new RuntimeException("Cannot Follow Yourself");
        }
        Users users = usersRepository.findByIdIsDeletedFalse(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        Users targetUsers = usersRepository.findByIdIsDeletedFalse(targetUsersId).orElseThrow(() ->
                new RuntimeException("Target User Not Found"));
        followRepository.save(Follow.of(users, targetUsers));
    }

    @Override
    public void unfollow(Integer usersId, Integer targetUsersId) {
        if(usersId.equals(targetUsersId)) {
            throw new RuntimeException("Cannot Follow Yourself");
        }
        Users users = usersRepository.findByIdIsDeletedFalse(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        Users targetUsers = usersRepository.findByIdIsDeletedFalse(targetUsersId).orElseThrow(() ->
                new RuntimeException("Target User Not Found"));
        followRepository.delete(Follow.of(users, targetUsers));
    }

    @Override
    public PagingResultDto<UsersListResponseDto> followerList(Integer usersId, Integer startOffset, Integer pageSize) {
        pageSize = verifyFollowPageSize(pageSize);
        startOffset = verifyFollowStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);
        Page<Follow> followers = followRepository.findFollowersByUsersIdDest(pageable, usersId);
        List<UsersListResponseDto> result = usersService.toUsersListResponseDto(
                followers.stream().map(Follow::getUserSrc).toList()
        );
        return new PagingResultDto<>(result, followers.getTotalElements());
    }

    @Override
    public PagingResultDto<UsersListResponseDto> followingList(Integer usersId, Integer startOffset, Integer pageSize) {
        pageSize = verifyFollowPageSize(pageSize);
        startOffset = verifyFollowStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);
        Page<Follow> followings = followRepository.findFollowingsByUsersIdSrc(pageable, usersId);
        List<UsersListResponseDto> result = usersService.toUsersListResponseDto(
                followings.stream().map(Follow::getUserSrc).toList()
        );
        return new PagingResultDto<>(result, followings.getTotalElements());
    }
}
