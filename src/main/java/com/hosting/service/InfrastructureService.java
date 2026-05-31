package com.hosting.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class InfrastructureService {

    @Async // 결제 승인 후 백그라운드에서 실행되도록 설정
    public void runProvisioningScript(Long requestId) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/opt/hosting/provisioner/create_hosting_server.sh",
                    String.valueOf(requestId)
            );
            pb.redirectErrorStream(true);
            pb.start();
            System.out.println("🚀 [자동화] 스크립트 실행 시작: request_id = " + requestId);
        } catch (IOException e) {
            System.err.println("❌ [에러] 스크립트 실행 실패: " + e.getMessage());
        }
    }
}