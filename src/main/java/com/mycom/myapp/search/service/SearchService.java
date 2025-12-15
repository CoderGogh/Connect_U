package com.mycom.myapp.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mycom.myapp.post.entity.PostEntity;
import com.mycom.myapp.post.repository.PostRepository;
import com.mycom.myapp.search.dto.SearchResultDto;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UsersRepository usersRepository;
    private final PostRepository postRepository;

    public SearchResultDto search(String type, String keyword, Pageable pageable) {

        if ("user".equalsIgnoreCase(type)) {
            return searchUsers(keyword, pageable);
        }

        if ("post".equalsIgnoreCase(type)) {
            return searchPosts(keyword, pageable);
        }

        throw new IllegalArgumentException("지원하지 않는 검색 타입입니다.");
    }

    private SearchResultDto searchUsers(String keyword, Pageable pageable) {
        Page<Users> usersPage =
                usersRepository.findByNickname(pageable, keyword);

        return SearchResultDto.fromUsers(usersPage);
    }

    private SearchResultDto searchPosts(String keyword, Pageable pageable) {
        Page<PostEntity> postPage =
            postRepository.searchByTitleOrContent(pageable, keyword);

        return SearchResultDto.fromPosts(postPage);
    }
}
