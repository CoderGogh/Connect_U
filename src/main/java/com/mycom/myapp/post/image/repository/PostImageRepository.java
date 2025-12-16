package com.mycom.myapp.post.image.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.mycom.myapp.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.entity.PostImageKey;

public interface PostImageRepository extends JpaRepository<PostImage, PostImageKey> {

    // 순서: 게시글별, isDeleted=false, imageKey IS NOT NULL, seq 순 정렬
    List<PostImage> findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(Post postEntity);

    // 프로젝션 기준: imageKey 리스트만 조회 (Storage 호출용으로 비용 절감)
    @Query("SELECT p.imageKey FROM PostImage p WHERE p.post = :postEntity AND p.isDeleted = false AND p.imageKey IS NOT NULL ORDER BY p.id.seq")
    List<String> findImageKeysByPost(@Param("postEntity") Post postEntity);

    // 현재 post에 대해 최대 seq 값을 조회 (없으면 -1 반환)
    @Query("SELECT COALESCE(MAX(p.id.seq), -1) FROM PostImage p WHERE p.post = :postEntity")
    Integer findMaxSeqByPost(@Param("postEntity") Post postEntity);

    // 논리 삭제 처리(Repository 책임 범위)
    @Modifying
    @Transactional
    @Query("UPDATE PostImage p SET p.isDeleted = true, p.deletedAt = :time WHERE p.id.postId = :postId AND p.id.seq = :seq")
    int markDeletedByPostIdAndSeq(@Param("postId") Integer postId, @Param("seq") Integer seq, @Param("time") LocalDateTime time);

    Optional<PostImage> findByPostAndIdSeq(Post post, Integer seq);
}
