package com.hosting.service;

import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
public class InfrastructureService {

    private final ServerRequestRepository serverRequestRepository;

    @Async
    @Transactional
    public void runProvisioningScript(Long requestId) {
        try {
            System.out.println("[AUTOMATION] 스크립트 시작: request_id=" + requestId);

            ProcessBuilder pb = new ProcessBuilder(
                    "/opt/hosting/provisioner/run_remote_provision.sh",
                    String.valueOf(requestId)
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 1. 스크립트 출력 로그 실시간 모니터링 (디버깅용)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[SCRIPT-LOG] " + line);
                }
            }

            // 2. 스크립트 종료 대기 (비동기 스레드이므로 메인 서버는 멈추지 않음)
            int exitCode = process.waitFor();

            // 3. 결과에 따른 DB 상태 업데이트
            ServerRequest request = serverRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("신청 내역 없음"));

            if (exitCode == 0) {
                System.out.println("[AUTOMATION] 스크립트 성공: request_id=" + requestId);
                request.setStatus("RUNNING"); // 구동 중으로 변경
            } else {
                System.err.println("[AUTOMATION] 스크립트 실패 (Exit Code: " + exitCode + ")");
                request.setStatus("ERROR"); // 에러 상태로 변경
                request.setErrorMsg("Provisioning failed with code: " + exitCode);
            }

            serverRequestRepository.save(request);

        } catch (Exception e) {
            System.err.println("[AUTOMATION] 예외 발생: " + e.getMessage());
            // 시스템 예외 시에도 에러 상태 기록
            serverRequestRepository.findById(requestId).ifPresent(r -> {
                r.setStatus("ERROR");
                serverRequestRepository.save(r);
            });
        }
    }
}