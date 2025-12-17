package com.mycom.myapp.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @GetMapping("/")
    public String homePage() {
        return "home/index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/join")
    public String joinPage() {
        return "auth/join";
    }

    @GetMapping("/mypage")
    public String myPage() {
        return "user/mypage";
    }

    @GetMapping("/mypage/edit")
    public String editProfilePage() {
        return "user/edit";
    }

    @GetMapping("/users/{id}")
    public String userProfile(@PathVariable("id") Long id, Model model) {
        model.addAttribute("userId", id);
        return "user/profile";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        return "search/results";
    }

    @GetMapping("/posts/new")
    public String createPostPage() {
        return "posts/create";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPostPage(@PathVariable("id") Long id, Model model) {
        model.addAttribute("postId", id);
        return "posts/edit";
    }
}
