package com.hosting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Service
public class AutomationService {

    @Async // 별도의 스레드에서 백그라운드로 실행되게 만듭니다.
    public void runProvisioningAsync(Long requestId) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/opt/hosting/provisioner/run_remote_provision.sh",
                    String.valueOf(requestId)
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[AUTOMATION LOG] {}", line);
                }
            }

            int exitCode = process.waitFor();
            log.info("[AUTOMATION] 스크립트 완료 / 종료 코드: {}", exitCode);

        } catch (Exception e) {
            log.error("[AUTOMATION] 스크립트 실행 실패 (requestId: {})", requestId, e);
        }
    }
}