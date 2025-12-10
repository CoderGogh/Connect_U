package com.mycom.myapp.comment.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.comment.like.entity.CommentLike;
import com.mycom.myapp.comment.like.entity.CommentLikeKey;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeKey> {
}
