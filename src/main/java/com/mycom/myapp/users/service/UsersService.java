package com.mycom.myapp.users.service;

import com.mycom.myapp.users.dto.UsersResponseDto;

public interface UsersService {
    UsersResponseDto getUsersById(Integer usersId);
    void quit(Integer usersId);
}
