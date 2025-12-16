package com.mycom.myapp.comment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.comment.dto.CommentCreateRequestDto;
import com.mycom.myapp.comment.dto.CommentResponseDto;
import com.mycom.myapp.comment.dto.CommentTreeResponseDto;
import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.like.entity.CommentLike;
import com.mycom.myapp.comment.like.repository.CommentLikeRepository;
import com.mycom.myapp.comment.repository.CommentRepository;
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


 
       // 댓글 생성
     
    @Override
    public CommentResponseDto createComment(CommentCreateRequestDto dto, Integer userId) {

        Post postEntity = postRepository.findById(dto.getPostId())
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
                .postEntity(postEntity)
                .users(user)
                .parentComment(parent)
                .content(dto.getContent())
                .build();

        Comment saved = commentRepository.save(comment);

        return convertToDto(saved, false);
    }


        //  댓글 페이징 + 트리 반환
    
    @Override
    public PagingResultDto<CommentTreeResponseDto> getCommentsByPost(
            Integer postId,
            Integer userId,
            Pageable pageable,
            String sort
    ) {
        Post postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        // ⭐ 여기서 정렬 적용
        Pageable sortedPageable = applySort(pageable, sort);

        // ⭐ 정렬된 pageable로 조회
        Page<Comment> parentPage = commentRepository
                .findParentCommentsForTree(postEntity, sortedPageable);


        List<Comment> parentComments = parentPage.getContent();

     // ⭐ postId 기준 전체 댓글 조회 (depth 제한 없음)
        List<Comment> allComments =
                commentRepository.findByPostEntityAndIsDeletedFalseOrderByCreatedAtAsc(postEntity);



        List<Integer> allIds = allComments.stream()
                .map(Comment::getId)
                .toList();

        List<CommentLike> likes = commentLikeRepository
                .findByIdUsersIdAndIdCommentIdIn(userId, allIds);

        Set<Integer> likedSet = likes.stream()
                .map(like -> like.getComment().getId())
                .collect(Collectors.toSet());
       
     // 부모 댓글 기준 페이징, 트리는 전체 댓글로 구성
        List<CommentTreeResponseDto> tree = buildTree(allComments, likedSet);
        Long totalCount =
        	    commentRepository.countParentCommentsByPost(postEntity);
        return new PagingResultDto<>(tree, totalCount);
    }


    private Pageable applySort(Pageable pageable, String sort) {

        return switch (sort) {
            case "like" -> PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("likeCount").descending()
            );

            default -> PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("createdAt").descending() // latest (기본)
            );
        };
    }



      //  댓글 수정

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


    
       //  댓글 삭제 (soft delete)      
    @Override
    public void deleteComment(Integer commentId, Integer userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (!comment.getUsers().getUsersId().equals(userId)) {
            throw new RuntimeException("작성자만 삭제 가능합니다.");
        }

        comment.softDelete();

        if (comment.getParentComment() != null) {
            comment.getParentComment().decreaseChildCount();
        }
    }



        // 내부 공통 DTO 변환 (단건)
 
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
   
        // TREE DTO 생성
      
    private CommentTreeResponseDto convertToTreeDto(Comment comment, boolean isLiked) {

        boolean deleted = Boolean.TRUE.equals(comment.getIsDeleted());

        String content = deleted ? "삭제된 댓글입니다." : comment.getContent();
        String username = deleted ? "알 수 없음" : comment.getUsers().getNickname();
        Integer userId = deleted ? null : comment.getUsers().getUsersId();

        return CommentTreeResponseDto.builder()
                .id(comment.getId())
                .content(content)
                .likeCount(comment.getLikeCount())
                .childCount(comment.getChildCount())
                .isDeleted(comment.getIsDeleted())
                .deletedAt(comment.getDeletedAt())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .userId(userId)
                .username(username)
                .parentCommentId(comment.getParentComment() != null ?
                        comment.getParentComment().getId() : null)
                .isLiked(isLiked)
                .children(new ArrayList<>())
                .build();
    }



      //   TREE 빌드
    
    private List<CommentTreeResponseDto> buildTree(
            List<Comment> comments,
            Set<Integer> likedSet
    ) {
        // 1) 모든 댓글 → Tree DTO 변환
        List<CommentTreeResponseDto> dtoList = comments.stream()
                .map(c -> convertToTreeDto(c, likedSet.contains(c.getId())))
                .toList();

        // 2) ID → DTO 매핑
        Map<Integer, CommentTreeResponseDto> map = dtoList.stream()
                .collect(Collectors.toMap(
                        CommentTreeResponseDto::getId,
                        dto -> dto
                ));

        // 3) 트리 구성
        List<CommentTreeResponseDto> roots = new ArrayList<>();

        for (CommentTreeResponseDto dto : dtoList) {
            Integer parentId = dto.getParentCommentId();

            if (parentId == null) {
                roots.add(dto);
            } else {
                CommentTreeResponseDto parent = map.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return roots;
    }

}

