package com.mycom.myapp.comment.repository;

import com.mycom.myapp.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
