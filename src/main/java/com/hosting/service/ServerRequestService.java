package com.hosting.service;

import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServerRequestService {

    private final ServerRequestRepository serverRequestRepository;

    @Transactional
    public ServerRequest createRequest(ServerRequest request) {
        // 1. 웹서버 가격 및 Flavor 계산
        request.setWebFlavor(calculateFlavor(request.getWebCpu(), request.getWebRam(), request.getWebStorage()));
        request.setWebPrice(calculatePrice(request.getWebCpu(), request.getWebRam(), request.getWebStorage()));

        // 2. DB서버 가격 및 Flavor 계산 (신청했을 경우만)
        if (request.isNeedDb()) {
            request.setDbFlavor(calculateFlavor(request.getDbCpu(), request.getDbRam(), request.getDbStorage()));
            request.setDbPrice(calculatePrice(request.getDbCpu(), request.getDbRam(), request.getDbStorage()));
        } else {
            request.setDbPrice(0);
        }

        // 3. 총 금액 계산
        request.setTotalPrice(request.getWebPrice() + request.getDbPrice());
        request.setStatus("PENDING");

        // 4. DB 저장
        ServerRequest savedRequest = serverRequestRepository.save(request);

        // 5. 서버 생성 스크립트 실행 (비동기)
        runCreateScript(savedRequest.getRequestId());

        return savedRequest;
    }

    // Flavor 이름 생성 규칙: c2.r4.d50
    private String calculateFlavor(int cpu, int ram, int storage) {
        return String.format("c%d.r%d.d%d", cpu, ram, storage);
    }

    // 가격 계산 규칙
    private int calculatePrice(int cpu, int ram, int storage) {
        int price = 0;
        if (cpu == 4) price += 10000; else if (cpu == 8) price += 25000;
        if (ram == 8) price += 12000; else if (ram == 16) price += 28000;
        if (storage == 100) price += 5000; else if (storage == 200) price += 15000;
        return price;
    }

    @Async // 별도 스레드에서 실행 (사용자는 기다리지 않음)
    public void runCreateScript(Long requestId) {
        try {
            ProcessBuilder pb = new ProcessBuilder("/opt/hosting/provisioner/create_hosting_server.sh", String.valueOf(requestId));
            pb.inheritIO(); // 로그를 스프링 부트 콘솔에서도 볼 수 있게 함
            pb.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}