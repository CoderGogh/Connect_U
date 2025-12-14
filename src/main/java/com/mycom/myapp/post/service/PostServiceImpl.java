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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UsersRepository usersRepository;

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

        // 이미지 저장 (JPA) - request.imageUrls가 있으면 PostImage 엔티티를 생성하여 저장
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int seq = 0; // ERD에 따르면 seq는 0 기반일 수 있음
            for (String url : request.getImageUrls()) {
                // imageKey로 URL을 그대로 사용, volume은 임시 0L
                PostImage img = PostImage.builder()
                        .post(saved)
                        .seq(seq)
                        .imageKey(url)
                        .volume(0L)
                        .build();
                postImageRepository.save(img);
                seq++;
            }
        }

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(Pageable pageable) {
        Page<Post> p = postRepository.findAll(pageable);
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
        postRepository.delete(post);
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
        java.util.List<PostImage> images = postImageRepository.findByPostOrderByIdSeq(post);
        java.util.List<com.mycom.myapp.post.dto.PostImageDto> imageDtos = images.stream().map(i -> {
            Integer seq = i.getId() != null ? i.getId().getSeq() : null;
            return new com.mycom.myapp.post.dto.PostImageDto(seq, i.getImageKey(), seq);
        }).collect(Collectors.toList());
        dto.setImages(imageDtos);
        return dto;
    }
}
