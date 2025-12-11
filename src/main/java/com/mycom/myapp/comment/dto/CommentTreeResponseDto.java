package com.mycom.myapp.comment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class CommentTreeResponseDto {

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

    // children
    @Builder.Default
    private List<CommentTreeResponseDto> children = new ArrayList<>();
    
    private Boolean isLiked;

}
