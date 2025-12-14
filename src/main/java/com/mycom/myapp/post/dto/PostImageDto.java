package com.mycom.myapp.post.dto;

public class PostImageDto {
    private Integer id;
    private String url;
    private Integer seq;

    public PostImageDto() {}

    public PostImageDto(Integer id, String url, Integer seq) {
        this.id = id;
        this.url = url;
        this.seq = seq;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }
}
