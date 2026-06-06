package com.hosting.controller;

import com.hosting.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/servers")
public class ServerApiController {

    private final ServerService serverService;

    // JDK 17 호환을 위해 꺾쇠괄호 사이에 공백 추가
    @GetMapping("/{requestId}/detail")
    public ResponseEntity<Map<String, Object> > getServerDetail(@PathVariable Long requestId) {
        // 서비스에서 가공된 웹/DB/접속정보 데이터를 가져와 반환
        return ResponseEntity.ok(serverService.getServerDetail(requestId));
    }
}