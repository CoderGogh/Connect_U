package com.mycom.myapp.comment.like.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.comment.like.entity.CommentLike;
import com.mycom.myapp.comment.like.entity.CommentLikeKey;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeKey> {
	
	
	List<CommentLike> findByIdUsersIdAndIdCommentIdIn(Integer usersId, List<Integer> commentIds);

}
