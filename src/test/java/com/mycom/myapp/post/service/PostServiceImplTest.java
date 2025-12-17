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
import com.mycom.myapp.storage.StorageClient;
import com.mycom.myapp.storage.UploadResult;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock PostRepository postRepository;
    @Mock PostImageRepository postImageRepository;
    @Mock UsersRepository usersRepository;
    @Mock FollowRepository followRepository;
    @Mock StorageClient storageClient;
    @Mock PostLikeRepository postLikeRepository;
    @InjectMocks PostServiceImpl postService;

    private Users buildUser(int id, String email) throws Exception {
        Users u = Users.builder()
                .email(email)
                .password("pw")
                .nickname("nick-" + id)
                .description("desc")
                .build();
        setField(u, "usersId", id);
        setField(u, "createdAt", LocalDateTime.now());
        return u;
    }

    private Post buildPost(int id, Users user, String title, String content) throws Exception {
        Post p = Post.builder()
                .users(user)
                .title(title)
                .content(content)
                .build();
        setField(p, "id", id);
        setField(p, "createdAt", LocalDateTime.now());
        return p;
    }

    @Test
    void createPost_saves_and_returns_dto() throws Exception {
        Users user = buildUser(1, "user@test.com");
        CreatePostRequest req = new CreatePostRequest();
        req.setTitle("t");
        req.setContent("c");
        Post saved = buildPost(10, user, "t", "c");
        given(usersRepository.findByIdIsDeletedFalse(1)).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willReturn(saved);
        given(postImageRepository.findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(saved)).willReturn(List.of());

        PostResponse dto = postService.createPost(req, 1);

        assertThat(dto.getId()).isEqualTo(10);
        assertThat(dto.getAuthorId()).isEqualTo(1);
        assertThat(dto.getTitle()).isEqualTo("t");
    }

    @Test
    void uploadPostImage_succeeds_when_authorized() throws Exception {
        Users user = buildUser(2, "owner@test.com");
        Post post = buildPost(5, user, "t", "c");
        MultipartFile file = mock(MultipartFile.class);
        Principal principal = () -> "owner@test.com";
        UploadResult uploadResult = mock(UploadResult.class);

        given(file.isEmpty()).willReturn(false);
        given(file.getOriginalFilename()).willReturn("a.png");
        given(file.getBytes()).willReturn("img".getBytes());
        given(uploadResult.getSize()).willReturn(123L);
        given(postRepository.findById(5)).willReturn(Optional.of(post));
        given(postImageRepository.findMaxSeqByPost(post)).willReturn(null);
        given(storageClient.upload(any(byte[].class), anyString())).willReturn(uploadResult);
        given(storageClient.getSignedUrl(anyString())).willReturn("signed-url");
        given(postImageRepository.save(any(PostImage.class))).willAnswer(invocation -> invocation.getArgument(0));

        PostImageDto dto = postService.uploadPostImage(5, file, principal);

        assertThat(dto.getSeq()).isEqualTo(0);
        assertThat(dto.getImageKey()).contains("posts/5/");
        assertThat(dto.getImageUrl()).isEqualTo("signed-url");
        verify(storageClient).upload(any(byte[].class), contains("posts/5/"));
    }

    @Test
    void uploadPostImage_throws_when_not_author() throws Exception {
        Users user = buildUser(2, "owner@test.com");
        Post post = buildPost(5, user, "t", "c");
        MultipartFile file = mock(MultipartFile.class);
        Principal principal = () -> "other@test.com";

        given(file.isEmpty()).willReturn(false);
        given(postRepository.findById(5)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.uploadPostImage(5, file, principal))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void uploadPostImage_throws_when_over_limit() throws Exception {
        Users user = buildUser(2, "owner@test.com");
        Post post = buildPost(5, user, "t", "c");
        MultipartFile file = mock(MultipartFile.class);

        given(file.isEmpty()).willReturn(false);
        given(postRepository.findById(5)).willReturn(Optional.of(post));
        given(postImageRepository.findMaxSeqByPost(post)).willReturn(4);

        assertThatThrownBy(() -> postService.uploadPostImage(5, file, () -> "owner@test.com"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void uploadPostImages_handles_multiple_files() throws Exception {
        Users user = buildUser(3, "u@test.com");
        Post post = buildPost(6, user, "t", "c");
        MultipartFile f1 = mock(MultipartFile.class);
        MultipartFile f2 = mock(MultipartFile.class);
        UploadResult uploadResult = mock(UploadResult.class);

        given(f1.isEmpty()).willReturn(false);
        given(f2.isEmpty()).willReturn(false);
        given(f1.getOriginalFilename()).willReturn("a.jpg");
        given(f2.getOriginalFilename()).willReturn("b.jpg");
        given(f1.getBytes()).willReturn("1".getBytes());
        given(f2.getBytes()).willReturn("2".getBytes());
        given(uploadResult.getSize()).willReturn(10L);
        given(postRepository.findById(6)).willReturn(Optional.of(post));
        given(postImageRepository.findMaxSeqByPost(post)).willReturn(null);
        given(storageClient.upload(any(byte[].class), anyString())).willReturn(uploadResult);
        given(storageClient.getSignedUrl(anyString())).willReturn("signed-1", "signed-2");
        given(postImageRepository.save(any(PostImage.class))).willAnswer(invocation -> invocation.getArgument(0));

        List<PostImageDto> result = postService.uploadPostImages(6, List.of(f1, f2), () -> "u@test.com");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSeq()).isEqualTo(0);
        assertThat(result.get(1).getSeq()).isEqualTo(1);
    }

    @Test
    void uploadPostImages_throws_when_exceeds_total_limit() throws Exception {
        Users user = buildUser(3, "u@test.com");
        Post post = buildPost(6, user, "t", "c");
        MultipartFile f1 = mock(MultipartFile.class);
        MultipartFile f2 = mock(MultipartFile.class);

        given(postRepository.findById(6)).willReturn(Optional.of(post));
        given(postImageRepository.findMaxSeqByPost(post)).willReturn(3); // already 4 images

        assertThatThrownBy(() -> postService.uploadPostImages(6, List.of(f1, f2), () -> "u@test.com"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deletePost_softDeletes_and_removes_images() throws Exception {
        Users user = buildUser(1, "user@test.com");
        Post post = buildPost(7, user, "t", "c");
        PostImage img = PostImage.builder()
                .post(post)
                .seq(0)
                .imageKey("posts/7/img.jpg")
                .volume(10L)
                .build();

        given(postRepository.findById(7)).willReturn(Optional.of(post));
        given(postImageRepository.findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(post))
                .willReturn(List.of(img));

        postService.deletePost(7, () -> "user@test.com");

        assertThat(post.getIsDeleted()).isTrue();
        assertThat(img.getIsDeleted()).isTrue();
        verify(storageClient).delete("posts/7/img.jpg");
        verify(postRepository).save(post);
        verify(postImageRepository).save(img);
    }

    @Test
    void getPost_returns_dto_with_signed_images() throws Exception {
        Users user = buildUser(1, "user@test.com");
        Post post = buildPost(8, user, "t", "c");
        PostImage img = PostImage.builder()
                .post(post)
                .seq(0)
                .imageKey("posts/8/img.jpg")
                .volume(10L)
                .build();

        given(postRepository.findById(8)).willReturn(Optional.of(post));
        given(postImageRepository.findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(post))
                .willReturn(List.of(img));
        given(storageClient.getSignedUrl("posts/8/img.jpg")).willReturn("signed");

        PostResponse dto = postService.getPost(8);

        assertThat(dto.getImages()).hasSize(1);
        assertThat(dto.getImages().get(0).getImageUrl()).isEqualTo("signed");
    }

    @Test
    void getPost_throws_when_not_found() {
        given(postRepository.findById(anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getPostsLatest_sets_isLiked_flag() throws Exception {
        Users user = buildUser(1, "user@test.com");
        Post p1 = buildPost(1, user, "t1", "c1");
        Post p2 = buildPost(2, user, "t2", "c2");
        Page<Post> page = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 2), 2);

        given(postRepository.findActiveOrderByCreatedAtDesc(any())).willReturn(page);
        given(postLikeRepository.findLikedPostIdList(List.of(1, 2), 1)).willReturn(Set.of(2));
        given(postImageRepository.findByPostAndIsDeletedFalseAndImageKeyIsNotNullOrderByIdSeq(any(Post.class)))
                .willReturn(List.of());

        PagingResultDto<PostResponse> result = postService.getPostsLatest(1, 0, 2);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().stream().filter(PostResponse::getIsLiked).map(PostResponse::getId))
                .containsExactly(2);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
