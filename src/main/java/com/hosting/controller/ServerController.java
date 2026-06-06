package com.hosting.controller;

import com.hosting.dto.ServerRequestDto;
import com.hosting.service.AutomationService;
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
    private final AutomationService automationService; // AutomationService 의존성 주입 추가

    @PostMapping("/servers/request")
    public String requestServer(@ModelAttribute ServerRequestDto dto, Principal principal) {

        // 1. DB 저장 처리 (이 줄이 끝나면 트랜잭션 Commit 완료)
        Long requestId = serverRequestService.createServerRequest(dto, principal.getName());

        // 2. 백그라운드로 자동화 스크립트 실행 지시 (지시만 하고 바로 다음 줄로 넘어감)
        automationService.runProvisioningAsync(requestId);

        // 3. 사용자에게는 대기 시간 없이 즉시 결과 화면 반환
        return "redirect:/servers/my";
    }
}