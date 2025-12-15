package com.mycom.myapp.search.dto;

import com.mycom.myapp.post.entity.PostEntity;
import com.mycom.myapp.users.entity.Users;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class SearchResultDto {

    private final String type;   // "user" | "post"
    private final List<?> results;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    private SearchResultDto(
            String type,
            List<?> results,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        this.type = type;
        this.results = results;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    // 사용자 검색 결과  

    public static SearchResultDto fromUsers(Page<Users> page) {
        return new SearchResultDto(
                "user",
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // 게시글 검색 결과 

    public static SearchResultDto fromPosts(Page<PostEntity> page) {
        return new SearchResultDto(
                "post",
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
