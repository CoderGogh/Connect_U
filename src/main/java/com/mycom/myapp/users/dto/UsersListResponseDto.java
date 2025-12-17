package com.mycom.myapp.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersListResponseDto {
    private Integer usersId;
    private String nickname;
    private String imageKey;
    private String imageUrl;
}
