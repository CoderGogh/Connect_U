package com.mycom.myapp.users.controller;

import com.mycom.myapp.annotation.CurrentUsersId;
import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.service.UsersService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UsersController {
    private final UsersService usersService;

    @GetMapping("/id/{usersId}")
    public ResponseEntity<UsersResponseDto> getUsersById(@PathVariable Integer usersId) {
        return ResponseEntity.ok(usersService.getUsersById(usersId));
    }

    @GetMapping("/my-info")
    public ResponseEntity<UsersResponseDto> getMyInfo(@CurrentUsersId Integer usersId) {
        return ResponseEntity.ok(usersService.getUsersById(usersId));
    }

    @DeleteMapping
    public ResponseEntity<Void> quit(HttpServletRequest request, @CurrentUsersId Integer usersId) throws ServletException {
        usersService.quit(request, usersId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<Void> update(HttpServletRequest request, @CurrentUsersId Integer usersId, @RequestBody UsersRequestDto usersRequestDto) throws ServletException {
        usersService.update(request, usersId, usersRequestDto);
        return ResponseEntity.ok().build();
    }
}
