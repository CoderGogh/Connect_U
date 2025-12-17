package com.mycom.myapp.post.like.service;

import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.like.repository.PostLikeRepository;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceImplTest {

    @Mock PostLikeRepository postLikeRepository;
    @Mock PostRepository postRepository;
    @Mock UsersRepository usersRepository;
    @InjectMocks PostLikeServiceImpl postLikeService;

    private Users buildUser(int id) throws Exception {
        Users u = Users.builder()
                .email("u" + id + "@t.com")
                .password("pw")
                .nickname("n" + id)
                .build();
        setField(u, "usersId", id);
        return u;
    }

    private Post buildPost(int id, Users user) throws Exception {
        Post p = Post.builder()
                .users(user)
                .title("t")
                .content("c")
                .build();
        setField(p, "id", id);
        setField(p, "createdAt", LocalDateTime.now());
        return p;
    }

    @Test
    void createLike_saves_and_increments() throws Exception {
        Users user = buildUser(1);
        Post post = buildPost(10, user);
        given(usersRepository.findById(1)).willReturn(Optional.of(user));
        given(postRepository.findById(10)).willReturn(Optional.of(post));

        postLikeService.createLike(1, 10);

        verify(postLikeRepository).saveAndFlush(any());
        assertThat(post.getLikeCount()).isEqualTo(1);
    }

    @Test
    void createLike_throws_on_duplicate() throws Exception {
        Users user = buildUser(1);
        Post post = buildPost(10, user);
        given(usersRepository.findById(1)).willReturn(Optional.of(user));
        given(postRepository.findById(10)).willReturn(Optional.of(post));
        given(postLikeRepository.saveAndFlush(any())).willThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> postLikeService.createLike(1, 10))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteLike_decrements_when_exists() throws Exception {
        Users user = buildUser(1);
        Post post = buildPost(10, user);
        setField(post, "likeCount", 2);
        given(postRepository.findById(10)).willReturn(Optional.of(post));
        given(postLikeRepository.deleteByPostIdAndUsersId(10, 1)).willReturn(1);

        postLikeService.deleteLike(1, 10);

        assertThat(post.getLikeCount()).isEqualTo(1);
    }

    @Test
    void deleteLike_throws_when_not_found() {
        given(postRepository.findById(anyInt())).willReturn(Optional.of(mock(Post.class)));
        given(postLikeRepository.deleteByPostIdAndUsersId(anyInt(), anyInt())).willReturn(0);

        assertThatThrownBy(() -> postLikeService.deleteLike(1, 2))
                .isInstanceOf(RuntimeException.class);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
