package com.mycom.myapp.users.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UsersResponseDto {
    private Integer usersId;
    private String email;
    private String nickname;
    private String description;
    private Boolean isFollowing; // 특정 유저 팔로우 여부(true: 팔로우 중, false: 팔로우 x, null: 자기 자신)
    private String imageKey;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> roles = new ArrayList<>();
}
