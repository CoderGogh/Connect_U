package com.mycom.myapp.post.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.entity.PostImageKey;

public interface PostImageRepository extends JpaRepository<PostImage, PostImageKey> {
}
