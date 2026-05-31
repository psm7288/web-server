package com.hosting.controller;

import com.hosting.entity.DbServerInfo;
import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.DbServerInfoRepository;
import com.hosting.service.MemberService;
import com.hosting.service.ServerRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final MemberService memberService;
    private final ServerRequestService serverRequestService;
    private final DbServerInfoRepository dbServerInfoRepository; // DB 조회용 리포지토리

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

            // 서비스 레이어에서 비즈니스 로직 처리
            ServerRequest result = serverRequestService.createRequest(request);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * [DB 서버 콘솔 URL 조회 API] - 수정된 버전
     * 스크립트 실행 대신 DB(db_server_info)에 저장된 URL을 즉시 반환합니다.
     */
    @GetMapping("/{serverId}/console-url") // GET 방식으로 변경
    public ResponseEntity<Map<String, String>> getConsoleUrl(
            @PathVariable Long serverId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 1. 현재 로그인 사용자 확인
            Member member = memberService.findByUsername(userDetails.getUsername());

            // 2. DB에서 해당 서버와 사용자에 매핑된 콘솔 URL 조회
            // DbServerInfoRepository에 findByServer_ServerIdAndMemberId 메서드가 있어야 함
            DbServerInfo dbInfo = dbServerInfoRepository
                    .findByServerIdAndMemberId(serverId, member.getMemberId()) // 메서드 이름 일치시킴
                    .orElseThrow(() -> new RuntimeException("해당 서버의 DB 정보를 찾을 수 없습니다."));
            // 3. 저장된 URL 반환
            return ResponseEntity.ok(Map.of("consoleUrl", dbInfo.getDbConsoleUrl()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "콘솔 주소를 조회하는 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}