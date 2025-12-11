package com.mycom.myapp.comment.repository;

import com.mycom.myapp.comment.entity.Comment;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // 게시글의 전체 댓글 조회 (삭제되지 않은 것만)
    List<Comment> findByPostAndIsDeletedFalseOrderByCreatedAtAsc(Post post);

    // 특정 댓글의 자식 댓글 조회
    List<Comment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(Comment parentComment);

    // 사용자가 작성한 댓글
    List<Comment> findByUsersAndIsDeletedFalseOrderByCreatedAtDesc(Users users);

    // parentId = null → 부모 댓글만 조회
    List<Comment> findByPostAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(Post post);
}
