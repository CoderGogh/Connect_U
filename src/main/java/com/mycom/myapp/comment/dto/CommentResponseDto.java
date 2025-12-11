package com.mycom.myapp.comment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto {

    private Integer id;
    private String content;
    private Integer likeCount;
    private Integer childCount;

    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer userId;
    private String username;

    private Integer parentCommentId;
    private Boolean isLiked;
}
