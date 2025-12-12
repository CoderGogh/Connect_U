package com.mycom.myapp.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.users.entity.Users;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

	// 게시글의 전체 댓글 조회 (삭제되지 않은 것만)
	List<Comment> findByPostAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

	// 특정 댓글의 자식 댓글 조회
	List<Comment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(Comment parentComment);

	// 사용자가 작성한 댓글
	List<Comment> findByUsersAndIsDeletedFalseOrderByCreatedAtDesc(Users users);

	// 부모 댓글만 조회
	List<Comment> findByPostAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

	// 부모 댓글 페이징 조회 메서드
	Page<Comment> findByPostAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Post post, Pageable pageable);
	
	
	List<Comment> findByParentCommentIdIn(List<Integer> parentIds);
}
