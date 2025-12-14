package com.mycom.myapp.post.repository;

import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.entity.PostImageKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("legacyPostImageRepository")
public interface PostImageRepository extends JpaRepository<PostImage, PostImageKey> {
}
