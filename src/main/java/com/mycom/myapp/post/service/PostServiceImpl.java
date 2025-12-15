package com.mycom.myapp.post.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.post.dto.*;
import com.mycom.myapp.post.entity.PostEntity;
import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.repository.PostImageRepository;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import com.mycom.myapp.storage.StorageClient;
import com.mycom.myapp.storage.UploadResult;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UsersRepository usersRepository;
    private final StorageClient storageClient;

    public PostServiceImpl(
            PostRepository postRepository,
            PostImageRepository postImageRepository,
            UsersRepository usersRepository,
            StorageClient storageClient
    ) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.usersRepository = usersRepository;
        this.storageClient = storageClient;
    }

    // ================= 게시글 목록 =================
    @Override
    @Transactional
    public PagingResultDto<PostResponse> listPosts(Pageable pageable) {
        Page<PostEntity> page = postRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);

        List<PostResponse> content = page.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        // 총 데이터 수를 page.getTotalElements()로 전달
        return new PagingResultDto<>(content, page.getTotalElements());
    }


    // ================= 게시글 생성 =================
    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest request, Principal principal) {
        if (principal == null) throw new IllegalArgumentException("Authentication required");

        Users user = usersRepository.findByEmailForLogin(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PostEntity post = PostEntity.builder()
                .users(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return toDto(postRepository.save(post));
    }

    // ================= 이미지 업로드 =================
    @Override
    @Transactional
    public PostImageDto uploadPostImage(Integer postId, MultipartFile file, Principal principal) throws Exception {
        if (storageClient == null) throw new IllegalStateException("StorageClient bean not configured");

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getIsDeleted()) throw new IllegalStateException("삭제된 게시글입니다.");
        if (principal == null || !principal.getName().equals(post.getUsers().getEmail()))
            throw new SecurityException("Not authorized");

        Integer maxSeq = postImageRepository.findMaxSeqByPost(post);
        int nextSeq = (maxSeq == null ? 0 : maxSeq + 1);

        if (nextSeq >= 5) throw new IllegalStateException("이미지는 최대 5장까지 업로드할 수 있습니다.");

        String imageKey = "post/" + postId + "/" + UUID.randomUUID();
        UploadResult uploadResult = storageClient.upload(file.getBytes(), imageKey);

        PostImage image = PostImage.builder()
                .post(post)
                .seq(nextSeq)
                .imageKey(imageKey)
                .volume(uploadResult.getSize())
                .build();

        postImageRepository.save(image);

        return new PostImageDto(
                image.getId().getSeq(),
                image.getImageKey(),
                image.getId().getSeq()
        );
    }

    // ================= 게시글 단건 조회 =================
    @Override
    @Transactional
    public PostResponse getPost(Integer id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return toDto(post);
    }

    // ================= 게시글 삭제 =================
    @Override
    @Transactional
    public void deletePost(Integer id, Principal principal) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUsers().getEmail().equals(principal.getName()))
            throw new SecurityException("Not authorized");

        post.softDelete();
        postRepository.save(post);
    }

    // ================= DTO 변환 =================
    private PostResponse toDto(PostEntity post) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setAuthorId(post.getUsers().getUsersId());
        dto.setAuthorUsername(post.getUsers().getNickname());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setLikeCount(post.getLikeCount());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setIsDeleted(post.getIsDeleted());
        dto.setDeletedAt(post.getDeletedAt());

        List<PostImageDto> images = postImageRepository
                .findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(post)
                .stream()
                .map(img -> new PostImageDto(
                        img.getId().getSeq(),
                        img.getImageKey(),
                        img.getId().getSeq()
                ))
                .toList();

        dto.setImages(images);
        return dto;
    }
}
