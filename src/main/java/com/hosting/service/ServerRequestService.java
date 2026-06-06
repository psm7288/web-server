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
        request.setWebFlavor(calculateFlavor(
                request.getWebCpu(),
                request.getWebRam(),
                request.getWebStorage()
        ));

        if (request.isNeedDb()) {
            request.setDbFlavor(calculateFlavor(
                    request.getDbCpu(),
                    request.getDbRam(),
                    request.getDbStorage()
            ));
        }

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

        if (request.getServerName() == null || request.getServerName().isBlank()) {
            if (request.getWebServerName() != null && !request.getWebServerName().isBlank()) {
                request.setServerName(request.getWebServerName() + "-pkg");
            } else {
                request.setServerName("hosting-pkg");
            }
        }

        if (request.isNeedDb()) {
            if (request.getDbServerName() == null || request.getDbServerName().isBlank()) {
                request.setDbServerName(request.getWebServerName() + "-db");
            }
        }

        request.setStatus("PENDING");
        request.setErrorMsg(null);

        ServerRequest savedRequest = serverRequestRepository.save(request);
        Long requestId = savedRequest.getRequestId();

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
}