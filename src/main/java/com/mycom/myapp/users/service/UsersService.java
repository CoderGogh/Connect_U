package com.mycom.myapp.users.service;

import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UsersService {
    UsersResponseDto getUsersById(Integer usersId);
    void quit(HttpServletRequest request, Integer usersId) throws ServletException;
    void update(HttpServletRequest request, Integer usersId, UsersRequestDto dto) throws ServletException;
    List<UsersListResponseDto> getUsersListByNickname(String nickname);
    List<UsersListResponseDto> toUsersListResponseDto(List<Users> usersList);
}
