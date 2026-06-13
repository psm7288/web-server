package com.hosting.controller;

import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import com.hosting.service.MemberService;
import com.hosting.service.ServerRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final MemberService memberService;
    private final ServerRequestRepository serverRequestRepository;
    private final ServerRequestService serverRequestService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard")
    public String dashboardHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());

            Member member = memberService.findByUsername(userDetails.getUsername());
            List<ServerRequest> myRequests = serverRequestRepository.findAllByMemberIdWithServers(member.getMemberId());

            model.addAttribute("myServers", myRequests);
            model.addAttribute("serverCount", myRequests.size());
        }
        return "dashboard/main";
    }

    @GetMapping("/servers/shop")
    public String shopPage() {
        return "dashboard/shop";
    }

    @PostMapping("/servers/request")
    @ResponseBody
    public ResponseEntity<?> requestServer(@RequestBody ServerRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
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

    @GetMapping("/servers/my")
    public String myServers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Member member = memberService.findByUsername(userDetails.getUsername());
        List<ServerRequest> myRequests = serverRequestRepository.findAllByMemberIdWithServers(member.getMemberId());
        model.addAttribute("serverList", myRequests);
        return "dashboard/my";
    }

    @GetMapping("/servers/detail/{requestId}")
    public String serverDetailPage(@PathVariable Long requestId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        ServerRequest request = serverRequestRepository.findById(requestId).orElse(null);

        if (request == null) {
            return "redirect:/servers/my?error=notfound";
        }

        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        }

        Map<String, Object> webServer = queryOne("""
                SELECT server_id, request_id, server_type, internal_ip, flavor, status
                FROM servers
                WHERE request_id = ? AND server_type = 'web'
                LIMIT 1
                """, requestId);

        Map<String, Object> dbServer = queryOne("""
                SELECT server_id, request_id, server_type, internal_ip, flavor, status
                FROM servers
                WHERE request_id = ? AND server_type = 'db'
                LIMIT 1
                """, requestId);

        Map<String, Object> webInfo = queryOne("""
                SELECT web_info_id, request_id, server_id, web_host, web_port, web_url,
                       ftp_host, ftp_port, ftp_username, ftp_password, ftp_path, created_at
                FROM web_server_info
                WHERE request_id = ?
                LIMIT 1
                """, requestId);

        Map<String, Object> dbInfo = queryOne("""
                SELECT db_info_id, request_id, server_id, db_name, db_username, db_password,
                       db_host, db_port, db_console_url, created_at
                FROM db_server_info
                WHERE request_id = ?
                LIMIT 1
                """, requestId);

        model.addAttribute("order", request);
        model.addAttribute("webServer", webServer);
        model.addAttribute("dbServer", dbServer);
        model.addAttribute("webInfo", webInfo);
        model.addAttribute("dbInfo", dbInfo);

        return "dashboard/detail";
    }

    private Map<String, Object> queryOne(String sql, Long requestId) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, requestId);
        return result.isEmpty() ? null : result.get(0);
    }

    @GetMapping("/api/servers/check-name")
    @ResponseBody
    public ResponseEntity<Boolean> checkDbName(@RequestParam(required = false) String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(serverRequestService.existsByDbServerName(name));
    }
}
