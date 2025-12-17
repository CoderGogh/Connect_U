package com.mycom.myapp.comment.like.service;

import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.like.entity.CommentLikeKey;
import com.mycom.myapp.comment.like.repository.CommentLikeRepository;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceImplTest {

    @Mock CommentRepository commentRepository;
    @Mock UsersRepository usersRepository;
    @Mock CommentLikeRepository commentLikeRepository;
    @InjectMocks CommentLikeServiceImpl commentLikeService;

    private Users buildUser(int id) throws Exception {
        Users u = Users.builder()
                .email("u" + id + "@t.com")
                .password("pw")
                .nickname("n" + id)
                .build();
        setField(u, "usersId", id);
        return u;
    }

    private Comment buildComment(int id, Users user) throws Exception {
        Comment c = Comment.builder()
                .users(user)
                .postEntity(null)
                .parentComment(null)
                .content("c")
                .build();
        setField(c, "id", id);
        return c;
    }

    @Test
    void toggleLike_adds_when_not_liked() throws Exception {
        Users user = buildUser(1);
        Comment comment = buildComment(10, user);
        given(commentRepository.findById(10)).willReturn(Optional.of(comment));
        given(usersRepository.findById(1)).willReturn(Optional.of(user));
        given(commentLikeRepository.existsById(new CommentLikeKey(10, 1))).willReturn(false);

        var dto = commentLikeService.toggleLike(10, 1);

        assertThat(dto.getIsLiked()).isTrue();
        assertThat(comment.getLikeCount()).isEqualTo(1);
        verify(commentLikeRepository).save(any());
    }

    @Test
    void toggleLike_removes_when_already_liked() throws Exception {
        Users user = buildUser(1);
        Comment comment = buildComment(10, user);
        setField(comment, "likeCount", 1);
        given(commentRepository.findById(10)).willReturn(Optional.of(comment));
        given(usersRepository.findById(1)).willReturn(Optional.of(user));
        given(commentLikeRepository.existsById(new CommentLikeKey(10, 1))).willReturn(true);

        var dto = commentLikeService.toggleLike(10, 1);

        assertThat(dto.getIsLiked()).isFalse();
        assertThat(comment.getLikeCount()).isEqualTo(0);
        verify(commentLikeRepository).deleteById(eq(new CommentLikeKey(10, 1)));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
