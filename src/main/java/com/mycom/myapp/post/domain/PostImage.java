// POJO

package com.mycom.myapp.post.domain;

public class PostImage {
    private Integer id;
    private String url;
    private Integer seq;
    private Post post;

    public PostImage() {}

    public PostImage(String url, Integer seq) {
        this.url = url;
        this.seq = seq;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
}
