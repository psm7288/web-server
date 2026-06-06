package com.hosting.controller;

import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import com.hosting.service.MemberService;
import com.hosting.service.ServerRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final MemberService memberService;
    private final ServerRequestRepository serverRequestRepository;
    private final ServerRequestService serverRequestService;

    // 1. 대시보드 홈 매핑 (404 해결)
    @GetMapping("/dashboard")
    public String dashboardHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());

            // 사용자별 신청 내역 조회 (main.html의 myServers 모델용)
            Member member = memberService.findByUsername(userDetails.getUsername());
            List<ServerRequest> myRequests = serverRequestRepository.findAllByMemberIdWithServers(member.getMemberId());

            model.addAttribute("myServers", myRequests);
            model.addAttribute("serverCount", myRequests.size());
        }
        return "dashboard/main"; // templates/dashboard/main.html 경로
    }

    // 2. 서버 신청 페이지
    @GetMapping("/servers/shop")
    public String shopPage() { return "dashboard/shop"; }

    // 3. 서버 신청 처리
    @PostMapping("/servers/request")
    @ResponseBody
    public ResponseEntity<?> requestServer(@RequestBody ServerRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Member member = memberService.findByUsername(userDetails.getUsername());
            request.setMember(member);
            serverRequestService.createRequest(request);
            return ResponseEntity.ok("{\"message\": \"success\"}");
        } catch (Exception e) {
            log.error("신청 실패", e);
            return ResponseEntity.status(500).body("{\"error\": \"failed\"}");
        }
    }

    // 4. 내 서버 목록 조회
    @GetMapping("/servers/my")
    public String myServers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Member member = memberService.findByUsername(userDetails.getUsername());
        List<ServerRequest> myRequests = serverRequestRepository.findAllByMemberIdWithServers(member.getMemberId());
        model.addAttribute("serverList", myRequests);
        return "dashboard/my";
    }

    // 5. 상세 페이지 조회
    @GetMapping("/servers/detail/{requestId}")
    public String serverDetailPage(@PathVariable Long requestId, Model model) {
        return serverRequestRepository.findById(requestId)
                .map(request -> {
                    model.addAttribute("order", request);
                    return "dashboard/detail";
                })
                .orElse("redirect:/servers/my?error=notfound");
    }

    // 6. 중복 체크 API
    @GetMapping("/api/servers/check-name")
    @ResponseBody
    public ResponseEntity<Boolean> checkDbName(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(serverRequestService.existsByDbServerName(name));
    }
}