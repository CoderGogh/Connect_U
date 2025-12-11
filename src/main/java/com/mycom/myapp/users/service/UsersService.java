package com.mycom.myapp.users.service;

import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

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

    public UsersResponseDto getUsersById(Integer usersId) {
        Users users = usersRepository.findByIdJoinRole(usersId).orElseThrow(() ->
                new RuntimeException("User Not Found"));
        return toUsersResponseDto(users);
    }
}
