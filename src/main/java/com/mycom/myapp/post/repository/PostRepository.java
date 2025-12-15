package com.mycom.myapp.post.repository;

import com.mycom.myapp.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("""
        select p
        from Post p
        where
            (p.title like %:keyword%
            or p.content like %:keyword%)
            and p.isDeleted = false
    """)
    Page<Post> searchByTitleOrContent(
            Pageable pageable,
            @Param("keyword") String keyword
    );

    /**
     * 게시글 전체 최신순 조회
     * @param pageable
     * @return
     */
    @Query("select p from Post p join fetch p.users u where p.isDeleted = false order by p.createdAt desc")
    Page<Post> findActiveOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 사용자들이 작성한 게시글들만 최신순 조회
     * @param pageable
     * @param usersIdList 특정 사용자들의 식별자 리스트
     * @return
     */
    @Query("select p from Post p join fetch p.users u where p.users.usersId in :usersIdList and p.isDeleted = false order by p.createdAt desc")
    Page<Post> findActiveFollwingPostsOrderByCreatedAtDesc(Pageable pageable, @Param("usersIdList") List<Integer> usersIdList);

    /**
     * 게시글 전체 좋아요 순 조회
     * @param pageable
     * @return
     */
    @Query("select p from Post p join fetch p.users u where u.isDeleted = false order by p.likeCount desc, p.createdAt desc")
    Page<Post> findActiveOrderByLikeCountDesc(Pageable pageable);

    /**
     * 특정 사용자들이 작성한 게시글만 좋아요순 조회
     * @param pageable
     * @param usersIdList 특정 사용자들의 식별자 리스트
     * @return
     */
    @Query("select p from Post p join fetch p.users u where p.users.usersId in :usersIdList and p.isDeleted = false order by p.likeCount desc, p.createdAt desc")
    Page<Post> findActiveFollowingPostsOrderByLikeCountDesc(Pageable pageable, @Param("usersIdList") List<Integer> usersIdList);
}
