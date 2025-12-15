package com.mycom.myapp.search.controller;

import com.mycom.myapp.search.dto.SearchResultDto;
import com.mycom.myapp.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public SearchResultDto search(
            @RequestParam("type") String type,
            @RequestParam("keyword") String keyword,
            Pageable pageable
    ) {
        return searchService.search(type, keyword, pageable);
    }
}
