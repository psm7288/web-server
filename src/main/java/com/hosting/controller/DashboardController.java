package com.hosting.controller;

import com.hosting.entity.ServerOrder;
import com.hosting.repository.ServerOrderRepository;
import com.hosting.service.InfrastructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal; // 💡 스프링 시큐리티 인증 유저를 가져오기 위해 추가
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ServerOrderRepository serverOrderRepository;

    @Autowired
    private InfrastructureService infrastructureService;

    // 💡 [핵심 교정] 스프링 시큐리티 환경에서 진짜 로그인한 사용자의 ID를 추출하는 메서드
    private String getLoginUsername(Principal principal) {
        if (principal == null) {
            // 혹시라도 로그인이 안 된 상태로 접근하면 임시 계정을 반환하되, 로그를 남깁니다.
            System.out.println("⚠️ [경고] 비인증 사용자가 인프라 시스템에 접근했습니다.");
            return "test_user";
        }
        // 시큐리티에 로그인된 진짜 아이디(예: audcks33)를 반환합니다.
        return principal.getName();
    }

    // 1. [GET] 대시보드 홈 화면 매핑
    @GetMapping("/dashboard")
    public String dashboardHome(Principal principal, Model model) {
        String currentLoginUser = getLoginUsername(principal);
        System.out.println("🔍 [대시보드 조회] 로그인된 사용자: " + currentLoginUser);

        model.addAttribute("username", currentLoginUser);

        // 자기가 생성한 서버 목록만 필터링
        List<ServerOrder> userOrders = serverOrderRepository.findByUsername(currentLoginUser);
        model.addAttribute("serverCount", userOrders.size());
        model.addAttribute("myServers", userOrders);
        return "dashboard/main";
    }

    // 2. [GET] 서버 상품 신청 화면 매핑
    @GetMapping("/servers/shop")
    public String serverShop(Principal principal, Model model) {
        model.addAttribute("username", getLoginUsername(principal));
        return "dashboard/shop";
    }

    // 3. [GET] 내 가상 서버 관리 화면 매핑
    @GetMapping("/servers/my")
    public String myServers(Principal principal, Model model) {
        String currentLoginUser = getLoginUsername(principal);
        model.addAttribute("username", currentLoginUser);

        List<ServerOrder> userServerList = serverOrderRepository.findByUsername(currentLoginUser);
        model.addAttribute("serverList", userServerList);
        return "dashboard/my";
    }

    // 4. [GET] 가상 서버 상세 페이지 조회 매핑
    @GetMapping("/servers/detail/{id}")
    public String serverDetail(@PathVariable("id") Long id, Principal principal, Model model) {
        String currentLoginUser = getLoginUsername(principal);
        model.addAttribute("username", currentLoginUser);

        ServerOrder order = serverOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 서버 패키지가 존재하지 않습니다. ID: " + id));

        // 다른 사람이 주소창에 ID 쳐서 들어오는 것 방지
        if (!order.getUsername().equals(currentLoginUser)) {
            return "redirect:/servers/my";
        }

        model.addAttribute("order", order);
        return "dashboard/detail";
    }

    // 5. [POST] 서버 자동화 생성 신청 수신 엔진
    @PostMapping("/servers/order")
    public String orderPackage(
            @RequestParam(value="productPackage", required=false, defaultValue="기본 풀스택 패키지") String productPackage,
            @RequestParam("webName") String webName,
            @RequestParam(value="webDesc", required=false, defaultValue="웹 서버 인프라 스택") String webDesc,
            @RequestParam(value="webCpu", required=false, defaultValue="2") int webCpu,
            @RequestParam(value="webRam", required=false, defaultValue="4") int webRam,
            @RequestParam(value="webStorage", required=false, defaultValue="50") int webStorage,
            @RequestParam("dbName") String dbName,
            @RequestParam(value="dbSchemaName", required=false, defaultValue="test_schema") String dbSchemaName,
            @RequestParam("dbUser") String dbUser,
            @RequestParam("dbPassword") String dbPassword,
            @RequestParam(value="dbCpu", required=false, defaultValue="2") int dbCpu,
            @RequestParam(value="dbRam", required=false, defaultValue="4") int dbRam,
            @RequestParam(value="dbStorage", required=false, defaultValue="100") int dbStorage,
            Principal principal) {

        String currentLoginUser = getLoginUsername(principal);

        ServerOrder order = new ServerOrder();
        order.setUsername(currentLoginUser); // 진짜 로그인한 유저명 주입!
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

        order.setStatus("RUNNING");
        order.setCreatedAt(LocalDateTime.now());

        ServerOrder savedOrder = serverOrderRepository.save(order);
        System.out.println("✅ [격리 저장] 유저 [" + currentLoginUser + "] 명의로 인프라 정상 등록 완료");

        try {
            infrastructureService.simulateOpenStackProvisioning(savedOrder.getId());
        } catch (Exception e) {
            System.out.println("인프라 시뮬레이션 연동 생략");
        }

        return "redirect:/servers/my";
    }

    // 6. [POST] 가상 서버 패키지 전체 삭제 엔진 (404 에러 원천 차단 경로 수정)
    @PostMapping("/servers/delete")
    public String deletePackage(@RequestParam("packageId") Long packageId, Principal principal) {
        String currentLoginUser = getLoginUsername(principal);
        ServerOrder order = serverOrderRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("해당 패키지가 없습니다. ID: " + packageId));

        // 본인 소유의 서버일 때만 삭제 처리 작동
        if (order.getUsername().equals(currentLoginUser)) {
            serverOrderRepository.deleteById(packageId);
            System.out.println("🗑️ [삭제 성공] 유저 " + currentLoginUser + "의 " + packageId + "번 패키지 영구 폐기");
        }

        return "redirect:/servers/my";
    }
}