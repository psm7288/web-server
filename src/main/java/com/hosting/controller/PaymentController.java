package com.hosting.controller;

import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import com.hosting.service.InfrastructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

        // 2. 자동화 스크립트 필수 기본값 명시적 설정 (NULL 방지)
        request.setImage("Ubuntu-24.04");
        request.setFlavor("c2.r4.d50");
        request.setNetworkName("selfservice");
        request.setKeyName("mykey");
        request.setTargetHost("compute-PowerEdge-T360");
        request.setRequestType("hosting");
        request.setStatus("PAID");
        request.setNeedDb(true);

        // serverName 및 web/db 서버 명칭 설정
        if (request.getWebServerName() != null && !request.getWebServerName().isEmpty()) {
            request.setServerName(request.getWebServerName() + "-pkg");
            request.setDbServerName(request.getWebServerName() + "-db");
        } else {
            request.setServerName("default-server-pkg");
            request.setDbServerName("default-db-pkg");
        }

        // 3. DB 저장 및 트랜잭션 커밋 대기
        serverRequestRepository.save(request);

        // 4. [핵심] 현재 트랜잭션이 완전히 '커밋'된 후 스크립트를 비동기로 실행하도록 등록
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 이 시점에는 완전히 커밋되었으므로, 스크립트 내부에서 정상 조회가 가능함
                // 메서드 이름을 runProvisioningScript로 일치시켰습니다.
                infrastructureService.runProvisioningScript(requestId);
            }
        });

        return "redirect:/dashboard?payment=success";
    }
}