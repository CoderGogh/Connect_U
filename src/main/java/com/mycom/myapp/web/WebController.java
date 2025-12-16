package com.mycom.myapp.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    public String userProfile(@PathVariable("id") Long id, HttpServletRequest request) {
        request.setAttribute("id", id);
        return "user/profile";
    }
}
