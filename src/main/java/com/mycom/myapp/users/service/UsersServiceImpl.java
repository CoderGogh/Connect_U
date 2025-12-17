package com.mycom.myapp.users.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.follow.repository.FollowRepository;
import com.mycom.myapp.storage.StorageClient;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private static final int USERS_MAX_PAGE_SIZE = 100;

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final StorageClient storageClient;
    private final FollowRepository followRepository;

    /**
     * Users -> UsersResponseDto
     */
    private UsersResponseDto toUsersResponseDto(Users users) {
        UsersResponseDto dto = new UsersResponseDto();
        dto.setUsersId(users.getUsersId());
        dto.setEmail(users.getEmail());
        dto.setNickname(users.getNickname());
        dto.setDescription(users.getDescription());
        dto.setImageKey(users.getImageKey());
        dto.setCreatedAt(users.getCreatedAt());
        dto.setUpdatedAt(users.getUpdatedAt());
        dto.setRoles(
                users.getUsersRoles()
                        .stream()
                        .map(ur -> ur.getRole().getName())
                        .toList()
        );

        if (users.getImageKey() != null) {
            dto.setImageUrl(storageClient.getSignedUrl(users.getImageKey()));
        }

        return dto;
    }

    /**
     * 페이지 크기 검증
     */
    private int verifyUsersPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return 1;
        if (pageSize > USERS_MAX_PAGE_SIZE) return USERS_MAX_PAGE_SIZE;
        return pageSize;
    }

    /**
     * 페이지 오프셋 검증
     */
    private int verifyUsersStartOffset(Integer startOffset) {
        if (startOffset == null || startOffset < 0) return 0;
        return startOffset;
    }

    /**
     * List<Users> -> List<UsersListResponseDto>
     */
    @Override
    public List<UsersListResponseDto> toUsersListResponseDto(List<Users> usersList) {
        List<UsersListResponseDto> result = new ArrayList<>();

        for (Users users : usersList) {
            UsersListResponseDto dto = new UsersListResponseDto();
            dto.setUsersId(users.getUsersId());
            dto.setNickname(users.getNickname());
            dto.setImageKey(users.getImageKey());

            if (users.getImageKey() != null) {
                dto.setImageUrl(storageClient.getSignedUrl(users.getImageKey()));
            }

            result.add(dto);
        }
        return result;
    }

    /**
     * 프로필 이미지 업로드
     */
    @Override
    @Transactional
    public UsersResponseDto uploadUsersImage(Integer usersId, MultipartFile file) throws Exception {

        Users users = usersRepository.findByIdIsDeletedFalse(usersId)
                .orElseThrow(() -> new RuntimeException("회원 정보가 존재하지 않습니다."));

        // 기존 이미지 삭제
        String oldImageKey = users.getImageKey();
        if (oldImageKey != null && !oldImageKey.isBlank()) {
            try {
                storageClient.delete(oldImageKey);
            } catch (Exception e) {
                // 실패해도 업로드는 진행
                e.printStackTrace();
            }
        }

        // 새 이미지 업로드
        String imageKey = "users/" + usersId + "/" + UUID.randomUUID();
        storageClient.upload(file.getBytes(), imageKey);

        // DB 반영
        users.updateImageKey(imageKey);

        return toUsersResponseDto(users);
    }

    /**
     * 유저 단건 조회
     */
    @Override
    public UsersResponseDto getUsersById(Integer usersId, Integer targetId) {

        Users users = usersRepository.findByIdJoinRole(targetId)
                .orElseThrow(() -> new RuntimeException("회원 정보가 존재하지 않습니다."));

        UsersResponseDto dto = toUsersResponseDto(users);

        // 자기 자신 조회
        if (usersId != null && usersId.equals(targetId)) {
            return dto;
        }

        // 팔로우 여부
        Integer followCount =
                followRepository.existsByUsersIdAndTargetId(usersId, targetId).orElse(0);

        dto.setIsFollowing(followCount != 0);
        return dto;
    }

    /**
     * 회원 탈퇴
     */
    @Override
    @Transactional
    public void quit(HttpServletRequest request, Integer usersId) throws ServletException {

        Users users = usersRepository.findByIdIsDeletedFalse(usersId)
                .orElseThrow(() -> new RuntimeException("회원 정보가 존재하지 않습니다."));

        // 프로필 이미지 삭제
        if (users.getImageKey() != null) {
            try {
                storageClient.delete(users.getImageKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        users.setDelete();
        request.logout();
    }

    /**
     * 회원 정보 수정
     */
    @Override
    @Transactional
    public void update(HttpServletRequest request, Integer usersId, UsersRequestDto dto)
            throws ServletException {

        Users users = usersRepository.findByIdIsDeletedFalse(usersId)
                .orElseThrow(() -> new RuntimeException("회원 정보가 존재하지 않습니다."));

        if (dto.getNickname() != null) {
            users.updateNickname(dto.getNickname());
        }
        if (dto.getDescription() != null) {
            users.updateDescription(dto.getDescription());
        }
        if (dto.getPassword() != null) {
            users.updatePassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        }
    }

    /**
     * 닉네임 검색
     */
    @Override
    public PagingResultDto<UsersListResponseDto> getUsersListByNickname(
            String nickname,
            Integer startOffset,
            Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(
                verifyUsersStartOffset(startOffset),
                verifyUsersPageSize(pageSize)
        );

        Page<Users> usersPage = usersRepository.findByNickname(pageable, nickname);

        return new PagingResultDto<>(
                toUsersListResponseDto(usersPage.getContent()),
                usersPage.getTotalElements()
        );
    }
}
