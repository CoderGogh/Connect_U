package com.mycom.myapp.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsersRequestDto {
    private String nickname;
    private String password;
    private String description;
}
