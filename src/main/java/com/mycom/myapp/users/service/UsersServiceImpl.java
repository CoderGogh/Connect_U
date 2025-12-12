package com.mycom.myapp.users.service;

import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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

    @Override
    public UsersResponseDto getUsersById(Integer usersId) {
        Users users = usersRepository.findByIdJoinRole(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        return toUsersResponseDto(users);
    }

    @Override
    public void quit(Integer usersId) {
        Users users = usersRepository.findById(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        users.setDelete();
        usersRepository.save(users);
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
}
