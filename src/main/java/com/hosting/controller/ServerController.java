package com.hosting.controller;

import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.service.MemberService;
import com.hosting.service.ServerRequestService; // 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final MemberService memberService;
    private final ServerRequestService serverRequestService; // 1. 서비스 주입 추가

    /**
     * [추가된 기능] 서버 신청 API
     * 사용자가 신청 폼을 보내면 가격을 계산하고 DB 저장 후 생성 스크립트를 깨웁니다.
     */
    @PostMapping("/requests")
    public ResponseEntity<ServerRequest> requestServer(
            @RequestBody ServerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 현재 로그인한 사용자 연결
            Member member = memberService.findByUsername(userDetails.getUsername());
            request.setMember(member);

            // 서비스에서 가격 계산 + DB 저장 + 스크립트 실행(비동기)을 한 번에 처리
            ServerRequest result = serverRequestService.createRequest(request);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * DB 서버 콘솔 URL 발급 (스크립트 기반) - 기존 유지
     */
    @PostMapping("/{serverId}/console")
    public ResponseEntity<Map<String, String>> getConsoleUrl(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Member member = memberService.findByUsername(userDetails.getUsername());

            ProcessBuilder pb = new ProcessBuilder(
                    "/opt/hosting/provisioner/get_console_url.sh",
                    String.valueOf(serverId),
                    String.valueOf(member.getMemberId())
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String url = reader.readLine();

            return ResponseEntity.ok(Map.of("consoleUrl", url));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "콘솔 주소를 가져오지 못했습니다: " + e.getMessage()));
        }
    }
}