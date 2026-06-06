package com.hosting.service;

import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerRequestService {

    private final ServerRequestRepository serverRequestRepository;
    private final InfrastructureService infrastructureService;

    @Transactional
    public ServerRequest createRequest(ServerRequest request) {
        // 1. 웹서버 flavor 계산
        request.setWebFlavor(calculateFlavor(
                request.getWebCpu(),
                request.getWebRam(),
                request.getWebStorage()
        ));

        // 2. DB서버 flavor 계산
        if (request.isNeedDb()) {
            request.setDbFlavor(calculateFlavor(
                    request.getDbCpu(),
                    request.getDbRam(),
                    request.getDbStorage()
            ));
        }

        // 3. 자동화 스크립트가 필요한 기본값 세팅
        if (request.getRequestType() == null || request.getRequestType().isBlank()) {
            request.setRequestType("hosting");
        }

        if (request.getImage() == null || request.getImage().isBlank()) {
            request.setImage("Ubuntu-24.04");
        }

        if (request.getFlavor() == null || request.getFlavor().isBlank()) {
            request.setFlavor(request.getWebFlavor());
        }

        if (request.getNetworkName() == null || request.getNetworkName().isBlank()) {
            request.setNetworkName("selfservice");
        }

        if (request.getKeyName() == null || request.getKeyName().isBlank()) {
            request.setKeyName("mykey");
        }

        if (request.getTargetHost() == null || request.getTargetHost().isBlank()) {
            request.setTargetHost("compute-PowerEdge-T360");
        }

        // 4. server_name NULL 방지
        if (request.getServerName() == null || request.getServerName().isBlank()) {
            if (request.getWebServerName() != null && !request.getWebServerName().isBlank()) {
                request.setServerName(request.getWebServerName() + "-pkg");
            } else {
                request.setServerName("hosting-pkg");
            }
        }

        // 5. DB 서버 이름 NULL 방지
        if (request.isNeedDb()) {
            if (request.getDbServerName() == null || request.getDbServerName().isBlank()) {
                request.setDbServerName(request.getWebServerName() + "-db");
            }
        }

        // 6. 상태값 초기화
        request.setStatus("PENDING");
        request.setErrorMsg(null);

        // 7. DB 저장
        ServerRequest savedRequest = serverRequestRepository.save(request);
        Long requestId = savedRequest.getRequestId();

        // 8. 중요: DB commit 이후 자동화 스크립트 실행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("[AUTOMATION] afterCommit 이후 자동화 실행 시작 - request_id={}", requestId);
                infrastructureService.runProvisioningScript(requestId);
            }
        });

        return savedRequest;
    }

    private String calculateFlavor(int cpu, int ram, int storage) {
        return String.format("c%d.r%d.d%d", cpu, ram, storage);
    }
    public boolean existsByDbServerName(String dbServerName) {
        return serverRequestRepository.existsByDbServerName(dbServerName);
    }
}