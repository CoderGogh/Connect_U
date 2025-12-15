package com.mycom.myapp.post.like.service;

import com.mycom.myapp.post.entity.PostEntity;
import com.mycom.myapp.post.like.entity.PostLike;
import com.mycom.myapp.post.like.repository.PostLikeRepository;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;

    @Override
    @Transactional
    public void createLike(Integer usersId, Integer postId) {
        Users users = usersRepository.findById(usersId).orElseThrow(() ->
                new RuntimeException("좋아요를 누른 회원 정보가 존재하지 않습니다."));
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() ->
                new RuntimeException("좋아요를 추가할 게시글 정보가 존재하지 않습니다."));
        PostLike postLike = PostLike.builder()
                .users(users)
            .postEntity(postEntity)
                .build();
        try {
            postLikeRepository.saveAndFlush(postLike); // 이 시점에 flush하도록 만들어 중복에 대한 예외 처리 수행
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("이미 좋아요를 표시한 게시글입니다.");
        }
        postEntity.increaseLikeCount();
    }

    @Override
    @Transactional
    public void deleteLike(Integer usersId, Integer postId) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        int deleted = postLikeRepository.deleteByPostIdAndUsersId(postId, usersId);

        if (deleted == 0) {
            throw new RuntimeException("이미 좋아요가 취소되었거나 존재하지 않습니다.");
        }

        if (post.getLikeCount() > 0) {
            post.decreaseLikeCount();
        }
    }

}
