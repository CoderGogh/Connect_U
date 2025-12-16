package com.mycom.myapp.search.dto;

import java.time.LocalDateTime;

import com.mycom.myapp.post.entity.Post;

import lombok.Getter;

@Getter
public class PostSearchResponseDto {

    private Integer postId;
    private String title;
    private String content;
    private Integer likeCount;
    private LocalDateTime createdAt;

    private Integer usersId;
    private String nickname;
    private String imageKey;

    private PostSearchResponseDto() {}

    public static PostSearchResponseDto from(Post post) {
        PostSearchResponseDto dto = new PostSearchResponseDto();
        dto.postId = post.getId();
        dto.title = post.getTitle();
        dto.content = post.getContent();
        dto.likeCount = post.getLikeCount();
        dto.createdAt = post.getCreatedAt();

        dto.usersId = post.getUsers().getUsersId();
        dto.nickname = post.getUsers().getNickname();
        dto.imageKey = post.getUsers().getImageKey();

        return dto;
    }
}
