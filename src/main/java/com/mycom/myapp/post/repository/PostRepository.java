package com.mycom.myapp.post.repository;

import com.mycom.myapp.post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {

    @Query("""
        select p
        from PostEntity p
        where
            (p.title like %:keyword%
            or p.content like %:keyword%)
            and p.isDeleted = false
    """)
    Page<PostEntity> searchByTitleOrContent(
            Pageable pageable,
            @Param("keyword") String keyword
    );

    Page<PostEntity> findByIsDeletedFalse(Pageable pageable);

    // findByIsDeletedFalseOrderByCreatedAtDesc
    Page<PostEntity> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}
