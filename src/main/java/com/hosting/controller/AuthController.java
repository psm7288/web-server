package com.hosting.controller;

import com.hosting.dto.SignupRequestDto;
import com.hosting.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        // HTML의 th:object="${signupForm}"과 이름이 똑같아야 합니다!
        model.addAttribute("signupForm", new SignupRequestDto());
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("signupForm") SignupRequestDto dto, Model model) {
        try {
            memberService.signup(dto);
            return "redirect:/login?signup=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "member/signup";
        }
    }

    // 아이디 중복 확인 (Ajax)
    @GetMapping("/signup/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean duplicated = memberService.isUsernameDuplicated(username);
        return ResponseEntity.ok(Map.of("duplicated", duplicated));
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String signup,
                            Model model) {
        if (error != null)  model.addAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
        if (logout != null) model.addAttribute("logoutMsg", "로그아웃 되었습니다.");
        if (signup != null) model.addAttribute("signupMsg", "회원가입이 완료되었습니다. 로그인 해주세요.");
        return "member/login";
    }
}
