package com.mycom.myapp.users.controller;

import com.mycom.myapp.auth.details.CustomUserDetails;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<UsersResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(usersService.getUsersById(principal.getId()));
    }

    @DeleteMapping
    public ResponseEntity<Void> quit(@AuthenticationPrincipal CustomUserDetails principal) {
        usersService.quit(principal.getId());
        return ResponseEntity.ok().build();
    }
}
