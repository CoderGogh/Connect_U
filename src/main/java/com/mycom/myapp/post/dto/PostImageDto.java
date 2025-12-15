package com.mycom.myapp.post.dto;

public class PostImageDto {

    private Integer id;   // seq 값
    private String url;   // imageKey
    private Integer seq;

    public PostImageDto(Integer id, String url, Integer seq) {
        this.id = id;
        this.url = url;
        this.seq = seq;
    }

    public Integer getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Integer getSeq() {
        return seq;
    }
}
