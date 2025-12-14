package com.mycom.myapp.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {

}
