package com.mycom.myapp.post.like.repository;

import com.mycom.myapp.post.like.entity.PostLike;
import com.mycom.myapp.post.like.entity.PostLikeKey;
import com.mycom.myapp.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeKey> {

    /**
     * 주어진 게시글 목록 내에서 특정 사용자가 좋아요를 누른 게시글의 식별자 목록 조회
     * @param postIdList
     * @param usersId
     * @return
     */
    @Query("select pl.postEntity.id from PostLike pl where pl.postEntity.id in :postIdList and pl.users.usersId = :usersId")
    Set<Integer> findLikedPostIdList(@Param("postIdList") List<Integer> postIdList, @Param("usersId") Integer usersId);

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
