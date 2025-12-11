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
    private String imageKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> roles = new ArrayList<>();
}
