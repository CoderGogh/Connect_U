package com.mycom.myapp.post.service;

import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostImageDto;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.repository.PostImageRepository;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.mycom.myapp.storage.StorageClient;
import com.mycom.myapp.storage.StorageException;
import com.mycom.myapp.storage.UploadResult;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UsersRepository usersRepository;
    @Autowired(required = false)
    private StorageClient storageClient;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, PostImageRepository postImageRepository, UsersRepository usersRepository) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest request, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        if (username == null) {
            throw new IllegalArgumentException("Authentication required");
        }

        Users user = usersRepository.findByEmailForLogin(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Post post = Post.builder()
                .users(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        Post saved = postRepository.save(post);
        return toDto(saved);
    }

    @Override
    @Transactional
    public PostImageDto uploadPostImage(Integer postId, MultipartFile file, Principal principal) throws Exception {
        if (storageClient == null) {
            throw new IllegalStateException("StorageClient bean not configured");
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

        // (선택) 권한 검사: 업로더가 게시글 작성자인지 확인
        String principalName = principal != null ? principal.getName() : null;
        if (principalName == null || !principalName.equals(post.getUsers().getEmail())) {
            throw new SecurityException("Not authorized to upload images for this post");
        }

        // seq 결정
        Integer maxSeq = postImageRepository.findMaxSeqByPost(post);
        int nextSeq = (maxSeq == null ? 0 : (maxSeq + 1));

        // imageKey 생성
        String imageKey = "post/" + post.getId() + "/" + UUID.randomUUID().toString();

        // Storage 업로드
        UploadResult res;
        try {
            res = storageClient.upload(file.getBytes(), imageKey);
        } catch (StorageException se) {
            throw se; // 호출자가 처리
        }

        // DB insert: 성공적으로 업로드된 경우에만 insert
        PostImage pi = PostImage.builder()
                .post(post)
                .seq(nextSeq)
                .imageKey(imageKey)
                .volume(res.getSize())
                .build();
        postImageRepository.save(pi);

        return new PostImageDto(nextSeq, imageKey, nextSeq);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(Pageable pageable) {
        Page<Post> p = postRepository.findByIsDeletedFalse(pageable);
        List<PostResponse> dtos = p.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, p.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(Integer id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("post not found"));
        return toDto(post);
    }

    @Override
    @Transactional
    public void deletePost(Integer id, Principal principal) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("post not found"));
        // 권한검사: Principal의 이름은 로그인 시 사용한 이메일(email)로 사용하므로
        // 게시글 작성자의 이메일과 비교하여 삭제 권한을 확인함.
        String principalName = principal != null ? principal.getName() : null;
        if (principalName == null || !principalName.equals(post.getUsers().getEmail())) {
            throw new SecurityException("Not authorized to delete this post");
        }
        // soft delete
        post.setIsDeleted(true);
        post.setDeletedAt(java.time.LocalDateTime.now());
        postRepository.save(post);
    }

    private PostResponse toDto(Post post) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        if (post.getUsers() != null) dto.setAuthorId(post.getUsers().getUsersId());
        if (post.getUsers() != null) dto.setAuthorUsername(post.getUsers().getNickname());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        // 이미지 매핑: PostImage 엔티티를 DTO로 변환
        java.util.List<PostImage> images = postImageRepository.findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(post);
        java.util.List<com.mycom.myapp.post.dto.PostImageDto> imageDtos = images.stream().map(i -> {
            Integer seq = i.getId() != null ? i.getId().getSeq() : null;
            return new com.mycom.myapp.post.dto.PostImageDto(seq, i.getImageKey(), seq);
        }).collect(Collectors.toList());
        dto.setImages(imageDtos);
        return dto;
    }
}
