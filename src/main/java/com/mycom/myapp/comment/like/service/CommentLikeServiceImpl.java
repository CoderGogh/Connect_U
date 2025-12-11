package com.mycom.myapp.comment.like.service;

import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.comment.like.entity.CommentLike;
import com.mycom.myapp.comment.like.entity.CommentLikeKey;
import com.mycom.myapp.comment.like.repository.CommentLikeRepository;
import com.mycom.myapp.comment.like.dto.CommentLikeResponseDto;
import com.mycom.myapp.comment.repository.CommentRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentRepository commentRepository;
    private final UsersRepository usersRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public CommentLikeResponseDto toggleLike(Integer commentId, Integer userId) {


        // 1. comment & user 조회

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        CommentLikeKey key = new CommentLikeKey(commentId, user.getUsersId());


        // 2. 기존 좋아요 여부 확인

        boolean isLiked = commentLikeRepository.existsById(key);

        if (isLiked) {
            // 좋아요 취소
            commentLikeRepository.deleteById(key);
            comment.decreaseLikeCount();

            return CommentLikeResponseDto.builder()
                    .commentId(commentId)
                    .likeCount(comment.getLikeCount())
                    .isLiked(false)
                    .build();
        } else {
            // 좋아요 추가
            CommentLike like = CommentLike.of(comment, user);
            commentLikeRepository.save(like);
            comment.increaseLikeCount();

            return CommentLikeResponseDto.builder()
                    .commentId(commentId)
                    .likeCount(comment.getLikeCount())
                    .isLiked(true)
                    .build();
        }
    }
}
