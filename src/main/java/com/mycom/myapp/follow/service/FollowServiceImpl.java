package com.mycom.myapp.follow.service;

import com.mycom.myapp.follow.entity.Follow;
import com.mycom.myapp.follow.repository.FollowRepository;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UsersRepository usersRepository;

    @Override
    public void follow(Integer usersId, Integer targetUsersId) {
        Users users = usersRepository.findByIdIsDeletedFalse(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        Users targetUsers = usersRepository.findByIdIsDeletedFalse(targetUsersId).orElseThrow(() ->
                new RuntimeException("Target User Not Found"));
        followRepository.save(Follow.of(users, targetUsers));
    }

    @Override
    public void unfollow(Integer usersId, Integer targetUsersId) {
        Users users = usersRepository.findByIdIsDeletedFalse(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        Users targetUsers = usersRepository.findByIdIsDeletedFalse(targetUsersId).orElseThrow(() ->
                new RuntimeException("Target User Not Found"));
        followRepository.delete(Follow.of(users, targetUsers));
    }

    @Override
    public List<UsersListResponseDto> followerList(Integer usersId) {
        return List.of();
    }

    @Override
    public List<UsersListResponseDto> followingList(Integer usersId) {
        return List.of();
    }
}
