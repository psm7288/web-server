package com.hosting.controller;

import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import com.hosting.service.InfrastructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final ServerRequestRepository serverRequestRepository;
    private final InfrastructureService infrastructureService;

    @PostMapping("/approve")
    public String approvePayment(@RequestParam("requestId") Long requestId) {
        // 1. 신청 내역 조회
        ServerRequest request = serverRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청 내역이 없습니다. ID: " + requestId));

        // 2. [핵심] 자동화 스크립트 실행을 위한 기본값 명시적 설정
        // DB에 NULL로 저장되는 것을 방지하고 스크립트가 읽어갈 데이터를 채웁니다.
        request.setImage("Ubuntu-24.04");
        request.setFlavor("c2.r4.d50");
        request.setNetworkName("selfservice");
        request.setKeyName("mykey");
        request.setTargetHost("compute-PowerEdge-T360");
        request.setRequestType("hosting");

        // serverName 설정: 폼에서 입력받은 웹 서버 이름에 -pkg 접미사 추가
        if (request.getWebServerName() != null && !request.getWebServerName().isEmpty()) {
            request.setServerName(request.getWebServerName() + "-pkg");
        } else {
            request.setServerName("default-server-pkg");
        }

        // 3. 상태 변경 및 업데이트된 정보 저장
        request.setStatus("PAID");
        serverRequestRepository.save(request);

        // 4. 서버 생성 자동화 스크립트 비동기 호출
        // 위에서 save를 먼저 수행했으므로, 스크립트는 업데이트된 값을 참조하게 됩니다.
        infrastructureService.runProvisioningScript(requestId);

        return "redirect:/dashboard?payment=success";
    }
}