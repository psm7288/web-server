package com.hosting.controller;

import com.hosting.entity.DbServerInfo;
import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.DbServerInfoRepository;
import com.hosting.repository.ServerRequestRepository; // 추가
import com.hosting.service.MemberService;
import com.hosting.service.ServerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final MemberService memberService;
    private final ServerRequestService serverRequestService;
    private final DbServerInfoRepository dbServerInfoRepository;
    private final ServerRequestRepository serverRequestRepository; // 중복 체크용 추가

    /**
     * [DB 서버 이름 중복 확인 API]
     * 프론트엔드의 중복 확인 버튼과 연동됩니다.
     */
    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkName(@RequestParam("name") String name) {
        // 리포지토리에서 실제 DB를 조회하여 결과 반환
        boolean exists = serverRequestRepository.existsByDbServerName(name);
        return ResponseEntity.ok(exists);
    }

    /**
     * [서버 신청 API]
     * 사용자가 신청 폼을 보내면 가격 계산 + DB 저장 + 생성 스크립트 실행(비동기)을 처리합니다.
     */
    @PostMapping("/requests")
    public ResponseEntity<ServerRequest> requestServer(
            @RequestBody ServerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Member member = memberService.findByUsername(userDetails.getUsername());
            request.setMember(member);

            // 서비스 레이어에서 비즈니스 로직 처리 (Status: PENDING)
            ServerRequest result = serverRequestService.createRequest(request);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * [DB 서버 콘솔 URL 조회 API]
     * Adminer 프록시 주소(/db-console/{id}/)를 즉시 반환합니다.
     */
    @GetMapping("/{serverId}/console-url")
    public ResponseEntity<Map<String, String>> getConsoleUrl(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Member member = memberService.findByUsername(userDetails.getUsername());

            // 보안 검증이 포함된 쿼리 호출
            DbServerInfo dbInfo = dbServerInfoRepository
                    .findByServerIdAndMemberId(serverId, member.getMemberId())
                    .orElseThrow(() -> new RuntimeException("해당 서버의 DB 정보를 찾을 수 없습니다."));

            return ResponseEntity.ok(Map.of("consoleUrl", dbInfo.getDbConsoleUrl()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "콘솔 주소를 조회하는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    // ServerController.java 에 추가
    @GetMapping("/requests/{requestId}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long requestId) {
        return serverRequestRepository.findById(requestId)
                .map(req -> {
                    Map<String, Object> statusMap = new HashMap<>();
                    statusMap.put("requestId", req.getRequestId());
                    statusMap.put("status", req.getStatus());
                    statusMap.put("errorMsg", req.getErrorMsg());
                    return ResponseEntity.ok(statusMap);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}