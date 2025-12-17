package com.mycom.myapp.users.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users API", description = "회원 관련 API")
public class UsersController {
    private final UsersService usersService;

    @GetMapping("/id/{usersId}")
    @Operation(summary = "식별자로 회원 세부 정보 조회")
    public ResponseEntity<UsersResponseDto> getUsersById(@CurrentUsersId(required = false) Integer myUsersId, @PathVariable("usersId") Integer usersId
) {
        return ResponseEntity.ok(usersService.getUsersById(myUsersId, usersId));
    }

    @GetMapping("/my-info")
    @Operation(summary = "자기 자신의 회원 세부 정보 조회")
    public ResponseEntity<UsersResponseDto> getMyInfo(@CurrentUsersId Integer usersId) {
        return ResponseEntity.ok(usersService.getUsersById(usersId, usersId));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "soft delete, 해당 유저의 세션을 무효화합니다.")
    public ResponseEntity<Void> quit(HttpServletRequest request, @CurrentUsersId Integer usersId) throws ServletException {
        usersService.quit(request, usersId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    @Operation(summary = "회원 정보 수정", description = "정보 수정 후 해당 유저의 세션을 무효화합니다. 다시 로그인해주세요.")
    public ResponseEntity<Void> update(HttpServletRequest request, @CurrentUsersId Integer usersId, @RequestBody UsersRequestDto usersRequestDto) throws ServletException {
        usersService.update(request, usersId, usersRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/images", consumes = "multipart/form-data")
    @Operation(summary = "회원 프로필 이미지(1개) 업로드")
    public ResponseEntity<UsersResponseDto> uploadImage(
            @RequestPart("file") MultipartFile file,
            @CurrentUsersId Integer usersId
    ) throws Exception {
        return ResponseEntity.ok(usersService.uploadUsersImage(usersId, file));
    }
}
