package com.mycom.myapp.post.like.repository;

import com.mycom.myapp.post.like.entity.PostLike;
import com.mycom.myapp.post.like.entity.PostLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeKey> {
}
