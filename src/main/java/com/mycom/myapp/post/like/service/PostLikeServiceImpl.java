package com.mycom.myapp.post.like.service;

import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.like.entity.PostLike;
import com.mycom.myapp.post.like.repository.PostLikeRepository;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;

    @Override
    public void createLike(Integer usersId, Integer postId) {
        Users users = usersRepository.findById(usersId).orElseThrow(() ->
                new RuntimeException("좋아요를 누른 회원 정보가 존재하지 않습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() ->
                new RuntimeException("좋아요를 추가할 게시글 정보가 존재하지 않습니다."));
        PostLike postLike = PostLike.builder()
                .users(users)
                .post(post)
                .build();
        try {
            postLikeRepository.save(postLike);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("이미 좋아요를 누른 게시글입니다.");
        }
    }

    @Override
    public void deleteLike(Integer usersId, Integer postId) {

    }
}
