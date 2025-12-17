package com.mycom.myapp.follow.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.follow.entity.Follow;
import com.mycom.myapp.follow.repository.FollowRepository;
import com.mycom.myapp.users.dto.UsersListResponseDto;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import com.mycom.myapp.users.service.UsersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock FollowRepository followRepository;
    @Mock UsersRepository usersRepository;
    @Mock UsersService usersService;
    @InjectMocks FollowServiceImpl followService;

    private Users buildUser(int id) throws Exception {
        Users u = Users.builder()
                .email("user" + id + "@test.com")
                .password("pw")
                .nickname("nick" + id)
                .build();
        setField(u, "usersId", id);
        setField(u, "createdAt", LocalDateTime.now());
        return u;
    }

    @Test
    void follow_saves_relation() throws Exception {
        Users me = buildUser(1);
        Users target = buildUser(2);
        given(usersRepository.findByIdIsDeletedFalse(1)).willReturn(Optional.of(me));
        given(usersRepository.findByIdIsDeletedFalse(2)).willReturn(Optional.of(target));

        followService.follow(1, 2);

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void follow_throws_when_self_follow() {
        assertThatThrownBy(() -> followService.follow(1, 1))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void unfollow_deletes_relation() throws Exception {
        Users me = buildUser(1);
        Users target = buildUser(2);
        given(usersRepository.findByIdIsDeletedFalse(1)).willReturn(Optional.of(me));
        given(usersRepository.findByIdIsDeletedFalse(2)).willReturn(Optional.of(target));

        followService.unfollow(1, 2);

        verify(followRepository).delete(any(Follow.class));
    }

    @Test
    void followerList_returns_paging_result() throws Exception {
        Users me = buildUser(1);
        Users follower = buildUser(2);
        Follow follow = Follow.of(follower, me);
        Page<Follow> page = new PageImpl<>(List.of(follow), PageRequest.of(0, 10), 1);
        UsersListResponseDto dto = new UsersListResponseDto();

        given(followRepository.findFollowersByUsersIdDest(any(), eq(1))).willReturn(page);
        given(usersService.toUsersListResponseDto(any())).willReturn(List.of(dto));

        PagingResultDto<UsersListResponseDto> result = followService.followerList(1, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
    }

    @Test
    void followingList_returns_paging_result() throws Exception {
        Users me = buildUser(1);
        Users target = buildUser(3);
        Follow follow = Follow.of(me, target);
        Page<Follow> page = new PageImpl<>(List.of(follow), PageRequest.of(0, 10), 1);
        UsersListResponseDto dto = new UsersListResponseDto();

        given(followRepository.findFollowingsByUsersIdSrc(any(), eq(1))).willReturn(page);
        given(usersService.toUsersListResponseDto(any())).willReturn(List.of(dto));

        PagingResultDto<UsersListResponseDto> result = followService.followingList(1, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
