package com.mycom.myapp.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.users.entity.Users;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

	// 게시글의 전체 댓글 조회 (삭제되지 않은 것만)
	List<Comment> findByPostEntityAndIsDeletedFalseOrderByCreatedAtAsc(Post postEntity);

	// 특정 댓글의 자식 댓글 조회
	List<Comment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(Comment parentComment);

	// 사용자가 작성한 댓글
	List<Comment> findByUsersAndIsDeletedFalseOrderByCreatedAtDesc(Users users);


	// 페이징
	Page<Comment> findByPostEntityAndParentCommentIsNullAndIsDeletedFalse(
            Post postEntity, Pageable pageable
	);
	
	
	List<Comment> findByParentCommentIdIn(List<Integer> parentIds);
	
	List<Comment> findByPostEntityOrderByCreatedAtAsc(Post postEntity);

	
	    @Query("""
		    select c
		    from Comment c
		    where c.postEntity = :postEntity
		      and c.parentComment is null
		      and (c.isDeleted = false or c.childCount > 0)
		""")
		Page<Comment> findParentCommentsForTree(@Param("postEntity") Post postEntity, Pageable pageable);

	    @Query("""
	    	    select count(c)
	    	    from Comment c
	    	    where c.postEntity = :postEntity
	    	      and c.parentComment is null
	    	      and (c.isDeleted = false or c.childCount > 0)
	    	""")
	    	Long countParentCommentsByPost(@Param("postEntity") Post postEntity);
}
