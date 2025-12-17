package com.mycom.myapp.users.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.follow.repository.FollowRepository;
import com.mycom.myapp.storage.StorageClient;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.dto.UsersRequestDto;
import com.mycom.myapp.users.dto.UsersResponseDto;
import com.mycom.myapp.users.entity.Role;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.entity.UsersRole;
import com.mycom.myapp.users.repository.UsersRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceImplTest {

    @Mock UsersRepository usersRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock StorageClient storageClient;
    @Mock FollowRepository followRepository;
    @InjectMocks UsersServiceImpl usersService;

    private Users buildUser(int id) throws Exception {
        Users u = Users.builder()
                .email("user@test.com")
                .password("pw")
                .nickname("nick")
                .description("desc")
                .build();
        setField(u, "usersId", id);
        setField(u, "createdAt", LocalDateTime.now());
        setField(u, "updatedAt", LocalDateTime.now());
        return u;
    }

    private void addRole(Users user, String roleName) throws Exception {
        Role role = Role.of(roleName);
        setField(role, "roleId", 1);
        UsersRole ur = UsersRole.usersRoleof(user, role);
        setField(ur, "id", null); // not used in test assertions
    }

    @Test
    void uploadUsersImage_replaces_and_returns_url() throws Exception {
        Users user = buildUser(10);
        user.updateImageKey("old/key");
        MultipartFile file = mock(MultipartFile.class);
        given(file.getBytes()).willReturn("img".getBytes());
        given(usersRepository.findByIdIsDeletedFalse(10)).willReturn(Optional.of(user));
        given(storageClient.getSignedUrl(any())).willReturn("https://signed-url");

        UsersResponseDto dto = usersService.uploadUsersImage(10, file);

        assertThat(dto.getImageKey()).isNotNull();
        assertThat(dto.getImageUrl()).isEqualTo("https://signed-url");
        verify(storageClient).delete("old/key");
        verify(storageClient).upload(any(byte[].class), startsWith("users/10/"));
    }

    @Test
    void getUsersById_returns_self_without_follow_check() throws Exception {
        Users me = buildUser(10);
        me.updateImageKey("img");
        addRole(me, "ROLE_USER");
        given(usersRepository.findByIdJoinRole(10)).willReturn(Optional.of(me));
        given(storageClient.getSignedUrl(any())).willReturn("signed");

        UsersResponseDto dto = usersService.getUsersById(10, 10);

        assertThat(dto.getUsersId()).isEqualTo(10);
        assertThat(dto.getImageUrl()).isEqualTo("signed");
        verify(followRepository, never()).existsByUsersIdAndTargetId(any(), any());
    }

    @Test
    void getUsersById_sets_isFollowing_for_other_user() throws Exception {
        Users other = buildUser(20);
        other.updateImageKey("img");
        addRole(other, "ROLE_USER");
        given(usersRepository.findByIdJoinRole(20)).willReturn(Optional.of(other));
        given(storageClient.getSignedUrl(any())).willReturn("signed");
        given(followRepository.existsByUsersIdAndTargetId(10, 20)).willReturn(Optional.of(1));

        UsersResponseDto dto = usersService.getUsersById(10, 20);

        assertThat(dto.getUsersId()).isEqualTo(20);
        assertThat(dto.getIsFollowing()).isTrue();
    }

    @Test
    void quit_deletes_image_and_marks_deleted() throws Exception {
        Users u = buildUser(10);
        u.updateImageKey("old/key");
        given(usersRepository.findByIdIsDeletedFalse(10)).willReturn(Optional.of(u));
        HttpServletRequest request = mock(HttpServletRequest.class);

        usersService.quit(request, 10);

        verify(storageClient).delete("old/key");
        verify(request).logout();
        assertThat(u.getIsDeleted()).isTrue();
    }

    @Test
    void update_updates_fields_and_encodes_password() throws Exception {
        Users u = buildUser(10);
        given(usersRepository.findByIdIsDeletedFalse(10)).willReturn(Optional.of(u));
        given(passwordEncoder.encode("raw")).willReturn("encoded");

        UsersRequestDto dto = new UsersRequestDto();
        dto.setNickname("newNick");
        dto.setDescription("newDesc");
        dto.setPassword("raw");

        usersService.update(mock(HttpServletRequest.class), 10, dto);

        assertThat(u.getNickname()).isEqualTo("newNick");
        assertThat(u.getDescription()).isEqualTo("newDesc");
        assertThat(u.getPassword()).isEqualTo("encoded");
    }

    @Test
    void getUsersListByNickname_maps_to_dto_and_paging() throws Exception {
        Users u1 = buildUser(1);
        u1.updateImageKey("img1");
        Users u2 = buildUser(2);
        u2.updateImageKey("img2");
        Page<Users> page = new PageImpl<>(List.of(u1, u2), PageRequest.of(0, 2), 2);
        given(usersRepository.findByNickname(any(), eq("k"))).willReturn(page);
        given(storageClient.getSignedUrl(any())).willReturn("signed-url");

        PagingResultDto<UsersListResponseDto> result = usersService.getUsersListByNickname("k", 0, 2);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getImageUrl()).isEqualTo("signed-url");
        assertThat(result.getTotalCount()).isEqualTo(2);
    }

    @Test
    void uploadUsersImage_throws_when_not_found() {
        given(usersRepository.findByIdIsDeletedFalse(anyInt())).willReturn(Optional.empty());
        assertThatThrownBy(() -> usersService.uploadUsersImage(1, mock(MultipartFile.class)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getUsersById_throws_when_not_found() {
        given(usersRepository.findByIdJoinRole(anyInt())).willReturn(Optional.empty());
        assertThatThrownBy(() -> usersService.getUsersById(1, 2))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void quit_throws_when_not_found() {
        given(usersRepository.findByIdIsDeletedFalse(anyInt())).willReturn(Optional.empty());
        assertThatThrownBy(() -> usersService.quit(mock(HttpServletRequest.class), 1))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void update_throws_when_not_found() {
        given(usersRepository.findByIdIsDeletedFalse(anyInt())).willReturn(Optional.empty());
        assertThatThrownBy(() -> usersService.update(mock(HttpServletRequest.class), 1, new UsersRequestDto()))
                .isInstanceOf(RuntimeException.class);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
