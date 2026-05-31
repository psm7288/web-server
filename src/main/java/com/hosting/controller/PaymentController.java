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
        // 1. 상태를 PAID로 변경
        ServerRequest request = serverRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 신청 내역이 없습니다."));
        request.setStatus("PAID");
        serverRequestRepository.save(request);

        // 2. 서버 생성 자동화 스크립트 비동기 호출
        infrastructureService.runProvisioningScript(requestId);

        return "redirect:/dashboard?payment=success";
    }
}