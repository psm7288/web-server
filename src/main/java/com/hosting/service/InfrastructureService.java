package com.hosting.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class InfrastructureService {

    @Async
    public void runProvisioningScript(Long requestId) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/opt/hosting/provisioner/run_remote_provision.sh",
                    String.valueOf(requestId)
            );
            pb.redirectErrorStream(true);
            pb.start();
            System.out.println("자동화 스크립트 실행: request_id=" + requestId);
        } catch (Exception e) {
            System.err.println("자동화 스크립트 실행 실패: " + e.getMessage());
        }
    }

}