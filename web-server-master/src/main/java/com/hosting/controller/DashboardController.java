package com.hosting.controller;

import com.hosting.entity.ServerOrder;
import com.hosting.repository.ServerOrderRepository;
import com.hosting.service.InfrastructureService; // ✨ 1. 방금 만든 서비스 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ServerOrderRepository serverOrderRepository;

    @Autowired
    private InfrastructureService infrastructureService; // ✨ 2. 인프라 시뮬레이터 엔진 주입

    // 1. [GET] 대시보드 홈 화면 매핑
    @GetMapping("/dashboard")
    public String dashboardHome(Model model) {
        model.addAttribute("username", "테스트유저");
        List<ServerOrder> allOrders = serverOrderRepository.findAll();
        model.addAttribute("serverCount", allOrders.size());
        model.addAttribute("myServers", allOrders);
        return "dashboard/main";
    }

    // 2. [GET] 서버 상품 신청 화면 매핑
    @GetMapping("/servers/shop")
    public String serverShop(Model model) {
        model.addAttribute("username", "테스트유저");
        return "dashboard/shop";
    }

    // 3. [GET] 내 가상 서버 관리 화면 매핑
    @GetMapping("/servers/my")
    public String myServers(Model model) {
        model.addAttribute("username", "테스트유저");
        List<String> myServers = new ArrayList<>();
        myServers.add("임시패키지데이터");
        model.addAttribute("myServers", myServers);
        return "dashboard/my";
    }

    /**
     * 🛒 4. [POST] 서버 자동화 생성 신청 수신 엔진
     */
    @PostMapping("/servers/order")
    public String orderPackage(
            @RequestParam("productPackage") String productPackage,
            @RequestParam("webName") String webName,
            @RequestParam("webDesc") String webDesc,
            @RequestParam(value="webCpu", defaultValue="2") int webCpu,
            @RequestParam(value="webRam", defaultValue="4") int webRam,
            @RequestParam(value="webStorage", defaultValue="50") int webStorage,
            @RequestParam("dbName") String dbName,
            @RequestParam("dbSchemaName") String dbSchemaName,
            @RequestParam("dbUser") String dbUser,
            @RequestParam("dbPassword") String dbPassword,
            @RequestParam(value="dbCpu", defaultValue="2") int dbCpu,
            @RequestParam(value="dbRam", defaultValue="4") int dbRam,
            @RequestParam(value="dbStorage", defaultValue="100") int dbStorage,
            Model model) {

        ServerOrder order = new ServerOrder();
        order.setUsername("테스트유저");
        order.setWebName(webName);
        order.setWebDesc(webDesc);
        order.setWebCpu(webCpu);
        order.setWebRam(webRam);
        order.setWebStorage(webStorage);

        order.setDbName(dbName);
        order.setDbSchemaName(dbSchemaName);
        order.setDbUser(dbUser);
        order.setDbPassword(dbPassword);
        order.setDbCpu(dbCpu);
        order.setDbRam(dbRam);
        order.setDbStorage(dbStorage);

        order.setStatus("PENDING"); // 최초 상태는 대기중 세팅
        order.setCreatedAt(LocalDateTime.now());

        // A. 먼저 main-db에 PENDING 상태로 영구 저장합니다.
        ServerOrder savedOrder = serverOrderRepository.save(order);
        System.out.println("✅ [1단계 완료] main-db에 PENDING 상태로 주문 기록 성공!");

        // B. ✨ [핵심 조립] 저장된 주문 ID를 던져서 백엔드 비동기 오픈스택 빌더 가동!
        infrastructureService.simulateOpenStackProvisioning(savedOrder.getId());
        System.out.println("🚀 [2단계 완료] 백엔드 비동기 스레드에 인프라 빌더 서비스 이관 완료!");

        return "redirect:/dashboard";
    }

    
    // 5. [POST] 서버 운영 중 사양 변경 처리
    @PostMapping("/servers/upgrade")
    public String upgradeServer(
            @RequestParam("serverId") String serverId,
            @RequestParam("newCpu") int newCpu,
            @RequestParam("newRam") int newRam,
            @RequestParam("newStorage") String newStorage,
            Model model) {
        return "redirect:/servers/my";
    }

    // 6. [POST] 가상 서버 제어 (재부팅 / 정지) 수신 엔진
    @PostMapping("/servers/control")
    public String controlServer(
            @RequestParam("serverId") String serverId,
            @RequestParam("action") String action,
            Model model) {
        System.out.println("====== [가상 서버 제어 신호 수신] ======");
        System.out.println("▶ 대상 서버 UUID: " + serverId);
        System.out.println("▶ 실행 명령 액션: " + action.toUpperCase());
        System.out.println("========================================");
        return "redirect:/servers/my";
    }
}