package com.hosting.service;

import com.hosting.entity.ServerOrder;
import com.hosting.repository.ServerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class InfrastructureService {

    @Autowired
    private ServerOrderRepository serverOrderRepository;

    /**
     * 🚀 오픈스택 가상 머신 프로비저닝 시뮬레이터 (비동기 스레드로 작동)
     */
    @Async // 이 어노테이션이 붙으면 백엔드 뒷단에서 유저 몰래 독립적으로 실행됩니다.
    public void simulateOpenStackProvisioning(Long orderId) {
        try {
            System.out.println("▶ [오픈스택 엔진] 주문 ID " + orderId + "번 가상 인프라 생성 프로세스 가동...");

            // 기획서 흐름도 매핑: 가상 머신 생성 및 스크립트 실행 시뮬레이션 (5초 대기)
            Thread.sleep(5000);

            // 5초 뒤 생성이 완료되면 DB에서 해당 주문을 다시 꺼냅니다.
            ServerOrder order = serverOrderRepository.findById(orderId).orElse(null);

            if (order != null) {
                // 상태를 PENDING에서 RUNNING으로 변경!
                order.setStatus("RUNNING");

                System.out.println("▶ [오픈스택 엔진] 가상 네트워크 인프라 라우팅 및 가짜 IP 바인딩 완료.");

                // DB에 수정된 상태(RUNNING) 저장
                serverOrderRepository.save(order);

                System.out.println("✅ [오픈스택 엔진] 주문 ID " + orderId + "번 인프라 빌드 완료 ➡️ RUNNING 상태 전환 성공!");
            }

        } catch (InterruptedException e) {
            System.err.println("❌ 인프라 생성 중 에러 발생: " + e.getMessage());
        }
    }
}