package com.mycom.myapp.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostImageDto {

    private Integer seq;        // 이미지 순서
    private String imageKey;    // GCS object key
    private String imageUrl;    // public 접근 URL
}
