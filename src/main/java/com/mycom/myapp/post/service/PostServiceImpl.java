package com.mycom.myapp.post.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.follow.repository.FollowRepository;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostImageDto;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.repository.PostImageRepository;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.mycom.myapp.storage.StorageClient;
import com.mycom.myapp.storage.UploadResult;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final int POST_MAX_PAGE_SIZE = 100;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UsersRepository usersRepository;
    private final FollowRepository followRepository;
    private final StorageClient storageClient;

    /**
     * 게시글 페이징 시 페이지 크기 값 검증
     * @param pageSize
     * @return
     */
    private Integer verifyPostsPageSize(Integer pageSize) {
        if(pageSize < 1) {
            return 1;
        }
        if(pageSize > POST_MAX_PAGE_SIZE) {
            return POST_MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 게시글 페이징 시 페이지 번호 값 검증
     * @param startOffset
     * @return
     */
    private Integer verifyPostsStartOffset(Integer startOffset) {
        if(startOffset < 0) {
            return 0;
        }
        return startOffset;
    }

    // ================= 게시글 목록 =================
    @Override
    @Transactional
    public PagingResultDto<PostResponse> listPosts(Pageable pageable) {
        Page<Post> page = postRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);

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
    public PostResponse createPost(CreatePostRequest request, Integer usersId) {
        Users user = usersRepository.findByIdIsDeletedFalse(usersId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post post = Post.builder()
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

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getIsDeleted()) throw new IllegalStateException("삭제된 게시글입니다.");
        if (principal == null)  // || !principal.getName().equals(post.getUsers().getEmail()) 삭제 함 --> Controller/Security에 위임
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
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("post not found"));
        return toDto(post);
    }

    // ================= 게시글 삭제 =================
    @Override
    @Transactional
    public void deletePost(Integer id, Principal principal) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUsers().getEmail().equals(principal.getName()))
            throw new SecurityException("Not authorized");

        post.softDelete();
        postRepository.save(post);
    }

    private PostResponse toDto(Post post) {
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

    @Override
    public PagingResultDto<PostResponse> getFollwingPostLatest(Integer usersId, Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);

        List<Integer> followingUsersIdList = followRepository.findAllByUserSrc(usersId);
        Page<Post> followingPosts = postRepository.findActiveFollwingPostsOrderByCreatedAtDesc(pageable, followingUsersIdList);
        List<PostResponse> followingPostslist = followingPosts.stream().map(this::toDto).toList();

        return new PagingResultDto<>(followingPostslist, followingPosts.getTotalElements());
    }

    @Override
    public PagingResultDto<PostResponse> getPostsLatest(Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);

        // 비로그인 사용자의 경우 전체 최신순 조회만 제공
        Page<Post> posts = postRepository.findActiveOrderByCreatedAtDesc(pageable);
        List<PostResponse> list = posts.stream().map(this::toDto).toList();
        return new PagingResultDto<>(list, posts.getTotalElements());
    }

    @Override
    public PagingResultDto<PostResponse> getPostsLikesDesc(Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);
        Page<Post> posts = postRepository.findActiveOrderByLikeCountDesc(pageable);
        List<PostResponse> list = posts.stream().map(this::toDto).toList();
        return new PagingResultDto<>(list, posts.getTotalElements());
    }

    @Override
    public PagingResultDto<PostResponse> getFollwingPostLikesDesc(Integer usersId, Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);
        List<Integer> followingUsersIdList = followRepository.findAllByUserSrc(usersId);
        Page<Post> followingPosts = postRepository.findActiveFollowingPostsOrderByLikeCountDesc(pageable, followingUsersIdList);
        List<PostResponse> followingPostslist = followingPosts.stream().map(this::toDto).toList();
        return new PagingResultDto<>(followingPostslist, followingPosts.getTotalElements());
    }

    @Override
    public PagingResultDto<PostResponse> getPostListByKeyword(String keyword, Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);

        Page<Post> postPage = postRepository.searchByTitleOrContent(pageable, keyword);
        List<PostResponse> list = postPage.stream().map(this::toDto).toList();
        return new PagingResultDto<>(list, postPage.getTotalElements());
    }
}
