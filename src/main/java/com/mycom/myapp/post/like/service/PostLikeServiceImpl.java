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
import org.springframework.transaction.annotation.Transactional;

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
        postLikeRepository.save(postLike);
    }

    @Override
    @Transactional
    public void deleteLike(Integer usersId, Integer postId) {
        int ret = postLikeRepository.deleteByPostIdAndUsersId(postId, usersId);
        if(ret > 1) {
            // 좋아요 삭제 요청으로 실제 삭제된 레코드 수가 1개보다 많은 경우 롤백 & 예외 발생
            throw new RuntimeException("좋아요 취소 도중 서버에서 문제가 발생했습니다.");
        }
    }
}
