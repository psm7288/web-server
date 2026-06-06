package com.hosting.controller;

import com.hosting.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ... (기존 회원가입 화면, 로그인 화면 등 맵핑 로직들) ...

    /**
     * 아이디 중복 확인 API (Ajax 통신용)
     */
    @ResponseBody // HTML 화면이 아니라 데이터를 그대로 반환하도록 설정
    @GetMapping("/api/members/check-id")
    public ResponseEntity<Boolean> checkIdDuplicate(@RequestParam("username") String username) {

        // MemberService에 이미 만들어두신 메서드 호출!
        boolean isDuplicate = memberService.isUsernameDuplicated(username);

        return ResponseEntity.ok(isDuplicate);
    }
}