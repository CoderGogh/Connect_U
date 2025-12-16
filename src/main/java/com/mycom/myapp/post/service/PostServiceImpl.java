package com.mycom.myapp.post.service;

import com.mycom.myapp.common.PagingResultDto;
import com.mycom.myapp.follow.repository.FollowRepository;
import com.mycom.myapp.post.dto.CreatePostRequest;
import com.mycom.myapp.post.dto.PostImageDto;
import com.mycom.myapp.post.dto.PostResponse;
import com.mycom.myapp.post.entity.Post;
import com.mycom.myapp.post.image.entity.PostImage;
import com.mycom.myapp.post.image.repository.PostImageRepository;
import com.mycom.myapp.post.like.repository.PostLikeRepository;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import com.mycom.myapp.storage.StorageClient;
import com.mycom.myapp.storage.UploadResult;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Set;
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
    private final PostLikeRepository postLikeRepository;

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
    public PostImageDto uploadPostImage(
            Integer postId,
            MultipartFile file,
            Principal principal
    ) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // TODO: prod에서만 권한 체크(로컬 테스트용으로 하기 위함)
        if (principal != null) {
            if (!principal.getName().equals(post.getUsers().getEmail())) {
                throw new SecurityException("Not authorized");
            }
        }


        // 이미지 개수 제한
        Integer maxSeq = postImageRepository.findMaxSeqByPost(post);
        int nextSeq = (maxSeq == null ? 0 : maxSeq + 1);
        if (nextSeq >= 5) {
            throw new IllegalStateException("이미지는 최대 5장까지 업로드할 수 있습니다.");
        }

        //  imageKey 생성
        String extension = extractExtension(file.getOriginalFilename());
        String imageKey = "posts/" + postId + "/" + UUID.randomUUID() + "." + extension;

        //  GCS 업로드
        UploadResult uploadResult = storageClient.upload(file.getBytes(), imageKey);

        //  DB 저장
        PostImage image = PostImage.builder()
                .post(post)
                .seq(nextSeq)
                .imageKey(imageKey)
                .volume(uploadResult.getSize())
                .build();

        postImageRepository.save(image);

        // Public URL 생성
        String imageUrl = storageClient.getPublicUrl(imageKey);


        return new PostImageDto(
                image.getId().getSeq(),
                imageKey,
                imageUrl
        );
    }


    // ================= 게시글 단건 조회 =================
    @Override
    @Transactional
    public PostResponse getPost(Integer id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("post not found"));
        return toDto(post);
    }

    // ================= 게시글 삭제(이미지 포함) =================
    @Override
    @Transactional
    public void deletePost(Integer id, Principal principal) {

        // 로그 확인용
        System.out.println("principal: " + principal);
        System.out.println("principal name: " + (principal != null ? principal.getName() : "null"));


        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUsers().getEmail().equals(principal.getName()))
            throw new SecurityException("Not authorized");

        // 1. 게시글에 연결된 이미지 조회
        List<PostImage> images = postImageRepository.findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(post);

        // 2. GCS에서 이미지 삭제
        for (PostImage img : images) {
            try {
                storageClient.delete(img.getImageKey());
            } catch (Exception e) {
                // 실패 시 로그 기록
                System.err.println("Failed to delete image from GCS: " + img.getImageKey());
                e.printStackTrace();
            }
        }

        // 3. 이미지 엔티티 soft delete
        for (PostImage img : images) {
            img.softDelete();
            postImageRepository.save(img);
        }

        // 4. 게시글 soft delete
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
                        storageClient.getPublicUrl(img.getImageKey())
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
        Set<Integer> likedPostIdSet = postLikeRepository.findLikedPostIdList(followingPosts.getContent().stream().map(Post::getId).toList(), usersId);
        List<PostResponse> followingPostslist = followingPosts.stream().map(this::toDto).toList();
        for(PostResponse p : followingPostslist){
            if(likedPostIdSet.contains(p.getId())) {
                p.setIsLiked(true);
            }
        }

        return new PagingResultDto<>(followingPostslist, followingPosts.getTotalElements());
    }

    @Override
    public PagingResultDto<PostResponse> getPostsLatest(Integer usersId, Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);

        // 비로그인 사용자의 경우 전체 최신순 조회만 제공
        Page<Post> posts = postRepository.findActiveOrderByCreatedAtDesc(pageable);
        Set<Integer> likedPostIdSet = postLikeRepository.findLikedPostIdList(posts.getContent().stream().map(Post::getId).toList(), usersId);
        List<PostResponse> list = posts.stream().map(this::toDto).toList();
        for(PostResponse p : list){
            if(likedPostIdSet.contains(p.getId())) {
                p.setIsLiked(true);
            }
        }
        return new PagingResultDto<>(list, posts.getTotalElements());
    }

    @Override
    public PagingResultDto<PostResponse> getPostsLikesDesc(Integer usersId, Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);
        Page<Post> posts = postRepository.findActiveOrderByLikeCountDesc(pageable);
        Set<Integer> likedPostIdSet = postLikeRepository.findLikedPostIdList(posts.getContent().stream().map(Post::getId).toList(), usersId);
        List<PostResponse> list = posts.stream().map(this::toDto).toList();
        for(PostResponse p : list){
            if(likedPostIdSet.contains(p.getId())) {
                p.setIsLiked(true);
            }
        }
        return new PagingResultDto<>(list, posts.getTotalElements());
    }

    @Override
    public PagingResultDto<PostResponse> getFollwingPostLikesDesc(Integer usersId, Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);
        List<Integer> followingUsersIdList = followRepository.findAllByUserSrc(usersId);
        Page<Post> followingPosts = postRepository.findActiveFollowingPostsOrderByLikeCountDesc(pageable, followingUsersIdList);
        Set<Integer> likedPostIdSet = postLikeRepository.findLikedPostIdList(followingPosts.getContent().stream().map(Post::getId).toList(), usersId);
        List<PostResponse> followingPostslist = followingPosts.stream().map(this::toDto).toList();
        for(PostResponse p : followingPostslist){
            if(likedPostIdSet.contains(p.getId())) {
                p.setIsLiked(true);
            }
        }
        return new PagingResultDto<>(followingPostslist, followingPosts.getTotalElements());
    }

    // 확장자 유틸 정의
    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "jpg"; // fallback
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase();
    }
    @Override
    @Transactional
    public PostResponse updatePost(
            Integer postId,
            CreatePostRequest request,
            List<Integer> deleteImageSeqs,
            List<MultipartFile> newImages,
            Principal principal
    ) throws Exception {
        // 1. 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 2. 권한 확인
        if (principal == null || !post.getUsers().getEmail().equals(principal.getName())) {
            throw new SecurityException("Not authorized");
        }

        // 3. 게시글 정보 수정
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        postRepository.save(post);

        // 4. 삭제할 이미지 처리
        if (deleteImageSeqs != null && !deleteImageSeqs.isEmpty()) {
            for (Integer seq : deleteImageSeqs) {
                PostImage image = postImageRepository.findByPostAndIdSeq(post, seq)
                        .orElseThrow(() -> new IllegalArgumentException("Image not found"));

                // GCS 삭제
                try {
                    storageClient.delete(image.getImageKey());
                } catch (Exception e) {
                    System.err.println("Failed to delete image from GCS: " + image.getImageKey());
                    e.printStackTrace();
                }

                // DB soft delete
                image.softDelete();
                postImageRepository.save(image);
            }
        }

        // 5. 새 이미지 업로드
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                uploadPostImage(postId, file, principal);
            }
        }

        // 6. DTO 반환
        return toDto(post);
    }



    @Override
    public PagingResultDto<PostResponse> getPostListByKeyword(Integer usersId, String keyword, Integer startOffset, Integer pageSize) {
        pageSize = verifyPostsPageSize(pageSize);
        startOffset = verifyPostsStartOffset(startOffset);
        Pageable pageable = PageRequest.of(startOffset, pageSize);

        Page<Post> postPage = postRepository.searchByTitleOrContent(pageable, keyword);
        Set<Integer> likedPostIdSet = postLikeRepository.findLikedPostIdList(postPage.getContent().stream().map(Post::getId).toList(), usersId);
        List<PostResponse> list = postPage.stream().map(this::toDto).toList();
        for(PostResponse p : list){
            if(likedPostIdSet.contains(p.getId())) {
                p.setIsLiked(true);
            }
        }
        return new PagingResultDto<>(list, postPage.getTotalElements());
    }
}
