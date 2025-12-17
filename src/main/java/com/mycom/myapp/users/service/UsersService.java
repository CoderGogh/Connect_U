package com.mycom.myapp.users.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UsersService {
    /**
     * 특정 유저의 정보 및 해당 유저 팔로우 여부를 조회
     * @param usersId 자신의 유저 식별자(필수 아님)
     * @param targetId 조회 대상 유저 식별자
     * @return
     */
    UsersResponseDto getUsersById(Integer usersId, Integer targetId);
    void quit(HttpServletRequest request, Integer usersId) throws ServletException;
    void update(HttpServletRequest request, Integer usersId, UsersRequestDto dto) throws ServletException;
    PagingResultDto<UsersListResponseDto> getUsersListByNickname(String nickname, Integer startOffset, Integer pageSize);
    List<UsersListResponseDto> toUsersListResponseDto(List<Users> usersList);
    String uploadUsersImage(
            Integer usersId,
            MultipartFile file
    ) throws Exception;
}
