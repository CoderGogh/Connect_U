package com.mycom.myapp.post.like.service;

public interface PostLikeService {
    void createLike(Integer usersId, Integer postId);

    /**
     * hard delete
     * @param postId
     */
    void deleteLike(Integer usersId, Integer postId);
}
