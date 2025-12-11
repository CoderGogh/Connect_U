package com.mycom.myapp.comment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentResponseDto;
import com.mycom.myapp.comment.dto.CommentTreeResponseDto;
import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.like.entity.CommentLike;
import com.mycom.myapp.comment.like.repository.CommentLikeRepository;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final CommentLikeRepository commentLikeRepository;


 
    @Override
    public CommentResponseDto createComment(CommentCreateRequestDto dto, Integer userId) {

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        Comment parent = null;
        if (dto.getParentCommentId() != null) {
            parent = commentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글이 존재하지 않습니다."));
            parent.increaseChildCount();
        }

        Comment comment = Comment.builder()
                .post(post)
                .users(user)
                .parentComment(parent)
                .content(dto.getContent())
                .build();

        Comment saved = commentRepository.save(comment);

        return convertToDto(saved, false);
    }


    @Override
    public List<CommentTreeResponseDto> getCommentsByPost(Integer postId, Integer userId) {

        // 1) 게시글 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        // 2) 게시글의 모든 댓글 조회
        List<Comment> comments = commentRepository
                .findByPostAndIsDeletedFalseOrderByCreatedAtAsc(post);

        // 댓글 ID 목록
        List<Integer> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        // 3) 좋아요 여부 조회 (현재 로그인 유저 기준)
        List<CommentLike> likes = commentLikeRepository
                .findByIdUsersIdAndIdCommentIdIn(userId, commentIds);

        Set<Integer> likedCommentIdSet = likes.stream()
                .map(like -> like.getComment().getId())
                .collect(Collectors.toSet());

        // 4) 트리 구조로 변환
        return buildTree(comments, likedCommentIdSet);
    }


    @Override
    public CommentResponseDto updateComment(Integer commentId, String content, Integer userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (!comment.getUsers().getUsersId().equals(userId)) {
            throw new RuntimeException("작성자만 수정 가능합니다.");
        }

        comment.updateContent(content);

        return convertToDto(comment, false);
    }

    @Override
    public void deleteComment(Integer commentId, Integer userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (!comment.getUsers().getUsersId().equals(userId)) {
            throw new RuntimeException("작성자만 삭제 가능합니다.");
        }

        // soft delete
        comment.softDelete();

        // 부모 댓글이면 childCount 감소
        if (comment.getParentComment() != null) {
            Comment parent = comment.getParentComment();
            parent.decreaseChildCount();
        }
    }


    private CommentResponseDto convertToDto(Comment comment, boolean isLiked) {

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .childCount(comment.getChildCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isDeleted(comment.getIsDeleted())
                .deletedAt(comment.getDeletedAt())
                .userId(comment.getUsers().getUsersId())
                .username(comment.getUsers().getNickname())
                .parentCommentId(
                        comment.getParentComment() != null ?
                                comment.getParentComment().getId() : null
                )
                .isLiked(isLiked)
                .build();
    }


    //         TREE DTO 생성
    private CommentTreeResponseDto toTreeDto(Comment comment, boolean isLiked) {

        return CommentTreeResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .childCount(comment.getChildCount())
                .isDeleted(comment.getIsDeleted())
                .deletedAt(comment.getDeletedAt())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .userId(comment.getUsers().getUsersId())
                .username(comment.getUsers().getNickname())
                .parentCommentId(
                        comment.getParentComment() != null ?
                                comment.getParentComment().getId() : null
                )
                .isLiked(isLiked)
                .children(new ArrayList<>())
                .build();
    }


    //     BUILD TREE STRUCTURE
    private List<CommentTreeResponseDto> buildTree(
            List<Comment> comments,
            Set<Integer> likedCommentIdSet
    ) {

        // 1) 모든 댓글을 Tree DTO로 변환
        List<CommentTreeResponseDto> dtoList = comments.stream()
                .map(comment -> toTreeDto(
                        comment,
                        likedCommentIdSet.contains(comment.getId())
                ))
                .toList();

        // 2) id → DTO 매핑
        Map<Integer, CommentTreeResponseDto> dtoMap =
                dtoList.stream().collect(Collectors.toMap(
                        CommentTreeResponseDto::getId,
                        dto -> dto
                ));

        // 3) 트리 구조 구성
        List<CommentTreeResponseDto> roots = new ArrayList<>();

        for (CommentTreeResponseDto dto : dtoList) {

            Integer parentId = dto.getParentCommentId();

            if (parentId == null) {
                roots.add(dto);
            } else {
                CommentTreeResponseDto parent = dtoMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return roots;
    }

}
