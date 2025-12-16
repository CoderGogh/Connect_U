package com.mycom.myapp.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostResponse {

    private Integer id;
    private Integer authorId;
    private String authorUsername;

    private String title;
    private String content;

    private Integer likeCount;
    private Boolean isLiked;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    private List<PostImageDto> images;
    private List<String> imageKeys;
}
