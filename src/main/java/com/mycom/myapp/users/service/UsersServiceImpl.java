package com.mycom.myapp.users.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {
    private final int USERS_MAX_PAGE_SIZE = 100;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * Users -> UsersResponseDto
     * @param users
     * @return
     */
    private UsersResponseDto toUsersResponseDto(Users users) {
        UsersResponseDto dto = new UsersResponseDto();
        dto.setUsersId(users.getUsersId());
        dto.setEmail(users.getEmail());
        dto.setDescription(users.getDescription());
        dto.setImageKey(users.getImageKey());
        dto.setNickname(users.getNickname());
        dto.setCreatedAt(users.getCreatedAt());
        dto.setUpdatedAt(users.getUpdatedAt());
        dto.setRoles(users.getUsersRoles().stream().map(ur -> ur.getRole().getName()).toList());
        return dto;
    }

    /**
     * 유저 페이징 시 페이지 크기 값 검증
     * @param pageSize
     * @return
     */
    private Integer verifyUsersPageSize(Integer pageSize) {
        if(pageSize < 1) {
            return 1;
        }
        if(pageSize > USERS_MAX_PAGE_SIZE) {
            return USERS_MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 유저 페이징 시 페이지 번호 값 검증
     * @param startOffset
     * @return
     */
    private Integer verifyUsersStartOffset(Integer startOffset) {
        if(startOffset < 0) {
            return 0;
        }
        return startOffset;
    }

    /**
     * List< Users > -> List< UsersListResponseDto >
     * @param usersList
     * @return
     */
    @Override
    public List<UsersListResponseDto> toUsersListResponseDto(List<Users> usersList) {
        List<UsersListResponseDto> list = new ArrayList<>();
        for (Users users : usersList) {
            UsersListResponseDto dto = new UsersListResponseDto();
            dto.setUsersId(users.getUsersId());
            dto.setNickname(users.getNickname());
            dto.setImageKey(users.getImageKey());
            list.add(dto);
        }
        return list;
    }

    @Override
    public UsersResponseDto getUsersById(Integer usersId) {
        Users users = usersRepository.findByIdJoinRole(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        return toUsersResponseDto(users);
    }

    @Override
    public void quit(HttpServletRequest request, Integer usersId) throws ServletException {
        Users users = usersRepository.findById(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        users.setDelete();
        usersRepository.save(users);
        request.logout();
    }

    @Override
    @Transactional
    public void update(HttpServletRequest request, Integer usersId, UsersRequestDto dto) throws ServletException {
        Users users = usersRepository.findByIdIsDeletedFalse(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        if(dto.getNickname() != null) {
            users.updateNickname(dto.getNickname());
        }
        if(dto.getDescription() != null) {
            users.updateDescription(dto.getDescription());
        }
        if(dto.getPassword() != null) {
            String encodedPassword = bCryptPasswordEncoder.encode(dto.getPassword());
            users.updatePassword(encodedPassword);
        }
        if(dto.getImageKey() != null) {
            users.updateImageKey(dto.getImageKey());
        }
        request.logout();
    }

    @Override
    public PagingResultDto<UsersListResponseDto> getUsersListByNickname(String nickname, Integer startOffset, Integer pageSize) {
        pageSize = verifyUsersPageSize(pageSize);
        startOffset = verifyUsersStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);
        Page<Users> usersList = usersRepository.findByNickname(pageable, nickname);
        List<UsersListResponseDto> result = toUsersListResponseDto(usersList.getContent());
        return new PagingResultDto<>(result, usersList.getTotalElements());
    }
}
