package com.mycom.myapp.comment.service;

import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentTreeResponseDto;
import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.like.entity.CommentLike;
import com.mycom.myapp.comment.like.entity.CommentLikeKey;
import com.mycom.myapp.comment.like.repository.CommentLikeRepository;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock UsersRepository usersRepository;
    @Mock CommentLikeRepository commentLikeRepository;
    @InjectMocks CommentServiceImpl commentService;

    private Users buildUser(int id) throws Exception {
        Users u = Users.builder()
                .email("u" + id + "@test.com")
                .password("pw")
                .nickname("nick" + id)
                .build();
        setField(u, "usersId", id);
        return u;
    }

    private Post buildPost(int id, Users user) throws Exception {
        Post p = Post.builder().users(user).title("t").content("c").build();
        setField(p, "id", id);
        setField(p, "createdAt", LocalDateTime.now());
        return p;
    }

    private Comment buildComment(int id, Post post, Users user, Comment parent, String content) throws Exception {
        Comment c = Comment.builder()
                .postEntity(post)
                .users(user)
                .parentComment(parent)
                .content(content)
                .build();
        setField(c, "id", id);
        setField(c, "createdAt", LocalDateTime.now());
        return c;
    }

    @Test
    void createComment_increments_parent_and_returns_dto() throws Exception {
        Users user = buildUser(1);
        Post post = buildPost(10, user);
        Comment parent = buildComment(100, post, user, null, "p");
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setPostId(10);
        dto.setParentCommentId(100);
        dto.setContent("child");
        Comment saved = buildComment(200, post, user, parent, "child");

        given(postRepository.findById(10)).willReturn(Optional.of(post));
        given(usersRepository.findById(1)).willReturn(Optional.of(user));
        given(commentRepository.findById(100)).willReturn(Optional.of(parent));
        given(commentRepository.save(any(Comment.class))).willReturn(saved);

        commentService.createComment(dto, 1);

        assertThat(parent.getChildCount()).isEqualTo(1);
    }

    @Test
    void updateComment_checks_author() throws Exception {
        Users user = buildUser(1);
        Users other = buildUser(2);
        Post post = buildPost(10, user);
        Comment comment = buildComment(1, post, user, null, "old");
        Comment otherComment = buildComment(2, post, other, null, "old");

        given(commentRepository.findById(1)).willReturn(Optional.of(comment));

        commentService.updateComment(1, "new", 1);
        assertThat(comment.getContent()).isEqualTo("new");

        given(commentRepository.findById(2)).willReturn(Optional.of(otherComment));
        assertThatThrownBy(() -> commentService.updateComment(2, "new", 1))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteComment_softDeletes_and_updates_parent() throws Exception {
        Users user = buildUser(1);
        Post post = buildPost(10, user);
        Comment parent = buildComment(5, post, user, null, "p");
        setField(parent, "childCount", 1);
        Comment child = buildComment(6, post, user, parent, "c");

        given(commentRepository.findById(6)).willReturn(Optional.of(child));

        commentService.deleteComment(6, 1);

        assertThat(child.getIsDeleted()).isTrue();
        assertThat(parent.getChildCount()).isEqualTo(0);
    }

    @Test
    void getCommentsByPost_builds_tree_and_liked_flag() throws Exception {
        Users user = buildUser(1);
        Post post = buildPost(10, user);
        Comment parent = buildComment(1, post, user, null, "p");
        Comment child = buildComment(2, post, user, parent, "c");
        Page<Comment> parentPage = new PageImpl<>(List.of(parent), PageRequest.of(0, 10), 1);

        given(postRepository.findById(10)).willReturn(Optional.of(post));
        given(commentRepository.findParentCommentsForTree(any(), any())).willReturn(parentPage);
        given(commentRepository.findByPostEntityOrderByCreatedAtAsc(post)).willReturn(List.of(parent, child));
        CommentLike like = CommentLike.of(child, user);
        given(commentLikeRepository.findByIdUsersIdAndIdCommentIdIn(1, List.of(1, 2)))
                .willReturn(List.of(like));
        given(commentRepository.countParentCommentsByPost(post)).willReturn(1L);

        PagingResultDto<CommentTreeResponseDto> result =
                commentService.getCommentsByPost(10, 1, PageRequest.of(0, 10), "latest");

        assertThat(result.getContent()).hasSize(1);
        CommentTreeResponseDto root = result.getContent().get(0);
        assertThat(root.getChildren()).hasSize(1);
        assertThat(root.getChildren().get(0).getIsLiked()).isTrue();
        assertThat(result.getTotalCount()).isEqualTo(1);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
