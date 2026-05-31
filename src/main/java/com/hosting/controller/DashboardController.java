package com.hosting.controller;

import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import com.hosting.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MemberService memberService;
    private final ServerRequestRepository serverRequestRepository;

    @GetMapping("/dashboard")
    public String dashboardHome(Model model) {
        // 대시보드 메인 페이지 HTML 이름 (예: dashboard/home.html)
        return "dashboard/main";
    }

    /**
     * [서버 상품 신청 페이지]
     * 주소: localhost:8080/servers/shop
     */
    @GetMapping("/servers/shop")
    public String shopPage(Model model) {
        // 우리가 확인한 파일 경로: templates/dashboard/shop.html
        return "dashboard/shop";
    }

    @GetMapping("/servers/my")
    public String myServers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        Member member = memberService.findByUsername(userDetails.getUsername());

        // 사용자의 서버 신청 목록 조회 (최신순)
        List<ServerRequest> myRequests = serverRequestRepository
                .findByMember_MemberIdOrderByCreatedAtDesc(member.getMemberId());

        model.addAttribute("username", member.getUsername());
        model.addAttribute("serverList", myRequests);

        return "dashboard/my"; // dashboard/my.html 렌더링
    }
}