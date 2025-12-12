package com.mycom.myapp.follow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequestDto {
    private Integer users_id_src;
    private Integer users_id_dest;
}
