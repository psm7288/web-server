package com.hosting.controller;

import com.hosting.service.OpenStackService; // 서비스 임포트 잊지 마세요!
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private final OpenStackService openStackService; // 서비스 주입

    // 기존 쇼핑 페이지
    @GetMapping("/shop")
    public String shopPage(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "shop";
    }

    // [추가된 코드] 특정 서버의 콘솔 URL을 가져오는 API
    @GetMapping("/{serverId}/console")
    @ResponseBody // HTML이 아니라 데이터(주소)만 보낸다는 뜻
    public ResponseEntity<String> getConsole(@PathVariable String serverId) {
        try {
            // 우리가 만든 OpenStackService의 함수 호출
            String consoleUrl = openStackService.getConsoleUrl(serverId);
            return ResponseEntity.ok(consoleUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("콘솔 주소를 가져오지 못했습니다: " + e.getMessage());
        }
    }
}