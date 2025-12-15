package com.mycom.myapp.post.like.repository;

import com.mycom.myapp.post.like.entity.PostLike;
import com.mycom.myapp.post.like.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    boolean existsByPostEntity_IdAndUsers_UsersId(Integer postId, Integer usersId);

    @Modifying
    @Query("""
        delete from PostLike pl
        where pl.postEntity.id = :postId
          and pl.users.usersId = :usersId
    """)
    int deleteByPostIdAndUsersId(
            @Param("postId") Integer postId,
            @Param("usersId") Integer usersId
    );
}
