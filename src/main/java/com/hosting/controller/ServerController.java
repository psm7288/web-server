package com.hosting.controller;

import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.service.MemberService;
import com.hosting.service.ServerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ServerController {

    private final ServerRequestService serverRequestService;
    private final MemberService memberService; // 사용자 정보를 가져오기 위해 주입

    @PostMapping("/servers/request")
    // DTO 대신 ServerRequest 엔티티를 바로 받아 폼 데이터를 자동 매핑합니다.
    public String requestServer(@ModelAttribute ServerRequest request, Principal principal) {

        // 1. 현재 로그인한 사용자 정보를 DB에서 조회하여 신청서에 세팅
        Member member = memberService.findByUsername(principal.getName());
        request.setMember(member);

        // 2. 서비스 단일 호출
        // (이 한 줄로 DB 저장 및 Commit 완료 직후 스크립트 자동 실행까지 완벽히 처리됨)
        serverRequestService.createRequest(request);

        // 3. 신청 완료 후 이동할 페이지
        return "redirect:/servers/my";
    }
}