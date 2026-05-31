package com.hosting.service;

import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그용 추가
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;

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

    @Async
    public void runCreateScript(Long requestId) {
        // [수정 포인트] 실제 스크립트 경로 (수민 님 환경에 맞춰 수정 가능)
        String scriptPath = "/opt/hosting/provisioner/create_hosting_server.sh";

        File scriptFile = new File(scriptPath);

        // 파일이 존재하는지 먼저 확인 (에러 방지 로직)
        if (!scriptFile.exists()) {
            log.error("❌ 스크립트 파일을 찾을 수 없습니다: {}", scriptPath);
            log.info("💡 로컬 환경(Mac) 테스트 중이라면 이 로그는 정상입니다. DB 저장은 완료되었습니다.");
            return; // 파일이 없으면 여기서 중단 (에러를 던지지 않음)
        }

        try {
            log.info("🚀 서버 생성 스크립트 실행 시작 (ID: {})", requestId);
            ProcessBuilder pb = new ProcessBuilder(scriptPath, String.valueOf(requestId));
            pb.inheritIO();
            Process process = pb.start();

            // 필요 시 프로세스 종료 대기 (비동기이므로 여기서 대기해도 사용자 응답엔 영향 없음)
            // int exitCode = process.waitFor();
            // log.info("✅ 스크립트 종료 코드: {}", exitCode);

        } catch (Exception e) {
            log.error("⚠️ 스크립트 실행 중 예외 발생: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}