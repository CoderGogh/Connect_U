package com.mycom.myapp.search.controller;

import com.mycom.myapp.search.dto.SearchResultDto;
import com.mycom.myapp.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Search API",
    description = "키워드 기반 사용자 / 게시글 통합 검색 API"
)
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(
        summary = "통합 검색",
        description = """
            키워드를 기준으로 사용자 또는 게시글을 검색합니다.

            - type=user : 사용자 닉네임 기준 검색
            - type=post : 게시글 제목 또는 본문 기준 검색
            - Pageable을 이용한 페이징 지원
            """
    )
    @GetMapping
    public SearchResultDto search(
            @Parameter(
                description = "검색 타입 (user | post)",
                example = "user",
                required = true
            )
            @RequestParam("type") String type,

            @Parameter(
                description = "검색 키워드",
                example = "spring",
                required = true
            )
            @RequestParam("keyword") String keyword,

            @Parameter(
                description = "페이징 정보 (page, size, sort)",
                hidden = true
            )
            Pageable pageable
    ) {
        return searchService.search(type, keyword, pageable);
    }
}
