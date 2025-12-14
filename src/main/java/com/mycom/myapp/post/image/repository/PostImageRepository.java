package com.mycom.myapp.post.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.entity.PostImageKey;

public interface PostImageRepository extends JpaRepository<PostImage, PostImageKey> {
	// 이미지 조회: 게시글별, 순번 정렬
	java.util.List<PostImage> findByPostOrderByIdSeq(Post post);
}
