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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

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

    // 모든 맵핑 메서드 실행 전 공통으로 모델에 값을 담아주는 기능
    @ModelAttribute
    public void addCommonAttributes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }
    }

    /**
     * [가상 서버 상세 관리 페이지]
     * 주소: localhost:8080/servers/detail/{requestId}
     */
    @GetMapping("/servers/detail/{requestId}")
    public String serverDetailPage(@PathVariable Long requestId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        if (userDetails == null) return "redirect:/login";

        // 1. 해당 신청 내역(requestId) 정보 조회
        ServerRequest serverRequest = serverRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청 내역이 없습니다. ID: " + requestId));

        // 2. 모델에 데이터 담기
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("order", serverRequest); // 상세 페이지에서 쓸 데이터

        // 3. 렌더링할 HTML 파일명 (templates/dashboard/detail.html이 있어야 함)
        return "dashboard/detail";
    }
}