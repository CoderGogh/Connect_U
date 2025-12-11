package com.mycom.myapp.comment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequestDto {

    private Integer postId;
    private Integer parentCommentId; // 대댓글이면 존재
    private String content;
}
