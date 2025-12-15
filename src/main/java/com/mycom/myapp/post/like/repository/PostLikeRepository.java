package com.mycom.myapp.post.like.repository;

import com.mycom.myapp.post.like.entity.PostLike;
import com.mycom.myapp.post.like.entity.PostLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeKey> {

    @Modifying
    @Query("delete from PostLike pl where pl.post.id = :postId and pl.users.usersId = :usersId")
    int deleteByPostIdAndUsersId(@Param("postId") Integer postId, @Param("usersId") Integer userId);
}
