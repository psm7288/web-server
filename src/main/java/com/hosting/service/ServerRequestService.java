package com.hosting.service;

import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그용 추가
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerRequestService {

    private final ServerRequestRepository serverRequestRepository;

    @Transactional
    public ServerRequest createRequest(ServerRequest request) {
        // 1. 웹서버 사양 및 가격 계산 (이미 Controller나 JS에서 넘어왔을 수 있지만 안전하게 재계산)
        request.setWebFlavor(calculateFlavor(request.getWebCpu(), request.getWebRam(), request.getWebStorage()));

        // 2. DB서버 사양 및 가격 계산
        if (request.isNeedDb()) {
            request.setDbFlavor(calculateFlavor(request.getDbCpu(), request.getDbRam(), request.getDbStorage()));
        }

        request.setStatus("PENDING");

        // 3. DB 저장
        ServerRequest savedRequest = serverRequestRepository.save(request);

        // 4. 서버 생성 스크립트 실행 (비동기)
        runCreateScript(savedRequest.getRequestId());

        return savedRequest;
    }

    private String calculateFlavor(int cpu, int ram, int storage) {
        return String.format("c%d.r%d.d%d", cpu, ram, storage);
    }

    // ServerRequestService.java (설계도 7번 반영)
    @Async
    public void runCreateScript(Long requestId) {
        String scriptPath = "/opt/hosting/provisioner/run_remote_provision.sh"; // 설계도 6번 래퍼 스크립트 경로
        File scriptFile = new File(scriptPath);

        if (!scriptFile.exists()) {
            log.error("❌ 래퍼 스크립트 없음: {}", scriptPath);
            return;
        }

        try {
            log.info("🚀 [AUTOMATION] 원격 생성 시작 - Request ID: {}", requestId);
            ProcessBuilder pb = new ProcessBuilder(scriptPath, String.valueOf(requestId));
            pb.redirectErrorStream(true); // 에러 스트림 통합

            Process process = pb.start();

            // 설계도 7번: 실시간 로그 읽기
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[AUTOMATION LOG] {}", line);
                }
            }

            int exitCode = process.waitFor();
            log.info("🏁 [AUTOMATION] 종료 코드: {}", exitCode);

        } catch (Exception e) {
            log.error("⚠️ 자동화 실행 중 치명적 오류: {}", e.getMessage());
        }
    }
}