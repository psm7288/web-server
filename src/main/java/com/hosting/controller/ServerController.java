package com.hosting.controller;

import com.hosting.entity.Member;
import com.hosting.service.MemberService;
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

    /**
     * DB 서버 콘솔 URL 발급 (스크립트 기반)
     */
    @PostMapping("/{serverId}/console")
    public ResponseEntity<Map<String, String>> getConsoleUrl(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 현재 로그인 사용자 정보 조회
            Member member = memberService.findByUsername(userDetails.getUsername());

            // get_console_url.sh <server_id> <member_id>
            ProcessBuilder pb = new ProcessBuilder(
                    "/opt/hosting/provisioner/get_console_url.sh",
                    String.valueOf(serverId),
                    String.valueOf(member.getMemberId())
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String url = reader.readLine(); // 스크립트 결과값(URL)

            return ResponseEntity.ok(Map.of("consoleUrl", url));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "콘솔 주소를 가져오지 못했습니다: " + e.getMessage()));
        }
    }
}