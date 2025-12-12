package com.mycom.myapp.users.service;

import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

public interface UsersService {
    UsersResponseDto getUsersById(Integer usersId);
    void quit(Integer usersId);
    void update(HttpServletRequest request, Integer usersId, UsersRequestDto dto) throws ServletException;
}
