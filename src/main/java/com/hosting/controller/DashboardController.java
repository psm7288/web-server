package com.hosting.controller;

import com.hosting.dto.ServerOrderDto;
import com.hosting.entity.ServerOrder;
import com.hosting.repository.ServerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    // 💡 중요: 프로젝트에 존재하는 'ServerOrderRepository'로 변수명을 일원화합니다.
    private final ServerOrderRepository serverOrderRepository;

    /**
     * 1. 대시보드 메인 홈 화면 매핑
     */
    @GetMapping("/dashboard")
    public String dashboardHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String currentLoginUser = userDetails.getUsername();
        List<ServerOrder> userOrders = serverOrderRepository.findByUsername(currentLoginUser);

        model.addAttribute("username", currentLoginUser);
        model.addAttribute("serverCount", userOrders.size());
        model.addAttribute("myServers", userOrders);

        return "dashboard/main"; // templates/dashboard/main.html
    }

    /**
     * 1.8 서버 상품 신청 화면 진입 (shop.html 열기)
     */
    @GetMapping("/servers/shop")
    public String serverShop(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", userDetails.getUsername());
        return "dashboard/shop"; // templates/dashboard/shop.html
    }

    /**
     * [신규] ⭐️ DB 서버 이름 비동기 중복 확인 API (Ajax 통신용)
     */
    @GetMapping("/servers/check-db-dup")
    @ResponseBody
    public boolean checkDbNameDuplicate(@RequestParam("dbName") String dbName) {
        Optional<ServerOrder> existingOrder = serverOrderRepository.findByDbName(dbName);
        return existingOrder.isPresent(); // 존재하면 true(중복), 없으면 false(사용 가능)
    }

    /**
     * 1.9 내 가상 서버 관리 화면 목록 진입
     */
    @GetMapping("/servers/my")
    public String myServers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String currentLoginUser = userDetails.getUsername();
        List<ServerOrder> myOrders = serverOrderRepository.findByUsername(currentLoginUser);

        model.addAttribute("username", currentLoginUser);
        model.addAttribute("serverList", myOrders);

        return "dashboard/my"; // templates/dashboard/my.html
    }

    /**
     * 2. 테이블 선택 브릿지 화면 (shop.html 양식 제출 시 이동)
     */
    @PostMapping("/servers/table-select")
    public String goTableSelectPage(@ModelAttribute("orderData") ServerOrderDto orderDto,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("orderData", orderDto);

        return "dashboard/table-select"; // templates/dashboard/table-select.html
    }

    /**
     * 3. 최종 인프라 신청 완료 처리 후 대시보드로 이동 (table-select.html에서 제출됨)
     */
    @PostMapping("/servers/order/complete")
    public String processFinalOrder(@ModelAttribute ServerOrderDto finalDto,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        String currentLoginUser = userDetails.getUsername();

        ServerOrder entity = new ServerOrder();
        entity.setUsername(currentLoginUser);
        entity.setWebName(finalDto.getWebName());
        entity.setWebDesc(finalDto.getWebDesc());
        entity.setWebCpu(finalDto.getWebCpu() == 0 ? 2 : finalDto.getWebCpu());
        entity.setWebRam(finalDto.getWebRam() == 0 ? 4 : finalDto.getWebRam());
        entity.setWebStorage(finalDto.getWebStorage() == 0 ? 50 : finalDto.getWebStorage());

        entity.setDbName(finalDto.getDbName());
        entity.setDbSchemaName(finalDto.getDbSchemaName());
        entity.setDbUser(finalDto.getDbUser());

        // 💡 엔티티에 맞춰 비밀번호 저장 활성화!
        entity.setDbPassword(finalDto.getDbPassword());

        entity.setDbCpu(finalDto.getDbCpu() == 0 ? 2 : finalDto.getDbCpu());
        entity.setDbRam(finalDto.getDbRam() == 0 ? 4 : finalDto.getDbRam());
        entity.setDbStorage(finalDto.getDbStorage() == 0 ? 100 : finalDto.getDbStorage());
        entity.setStatus("RUNNING");

        // 💡 신청 등록 일시 누락 버그 해결을 위해 현재 시간 주입!
        entity.setCreatedAt(java.time.LocalDateTime.now());

        serverOrderRepository.save(entity);

        return "redirect:/dashboard";
    }

    /**
     * 3.5 my.html 내부 서버 전원 제어 엔드포인트
     */
    @PostMapping("/servers/control")
    public String controlServer(@RequestParam("serverId") Long serverId,
                                @RequestParam("action") String action,
                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        String currentLoginUser = userDetails.getUsername();

        Optional<ServerOrder> orderOptional = serverOrderRepository.findById(serverId);
        if (orderOptional.isPresent()) {
            ServerOrder order = orderOptional.get();
            if (order.getUsername().equals(currentLoginUser)) {
                if ("terminate_web".equals(action) || "terminate_db".equals(action)) {
                    order.setStatus("PENDING");
                } else if ("reboot_web".equals(action) || "reboot_db".equals(action)) {
                    order.setStatus("RUNNING");
                }
                serverOrderRepository.save(order);
            }
        }
        return "redirect:/servers/my";
    }

    /**
     * 4. 가상 서버 인프라 상세 스펙 자원 관리 화면 (detail.html)
     */
    @GetMapping("/servers/detail/{id}")
    public String serverDetail(@PathVariable("id") Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        if (userDetails == null) return "redirect:/login";
        String currentLoginUser = userDetails.getUsername();

        Optional<ServerOrder> orderOptional = serverOrderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            return "redirect:/dashboard";
        }

        ServerOrder order = orderOptional.get();
        if (!order.getUsername().equals(currentLoginUser)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("username", currentLoginUser);
        model.addAttribute("order", order);

        return "dashboard/detail";
    }

    /**
     * 5. detail.html 스펙 변경 추가 확장(Scale-Up) 수신 엔드포인트
     */
    @PostMapping("/servers/upgrade")
    public String upgradeServerSpec(@RequestParam("packageId") Long packageId,
                                    @RequestParam("webCpu") int webCpu,
                                    @RequestParam("webRam") int webRam,
                                    @RequestParam("webStorage") int webStorage,
                                    @RequestParam("dbCpu") int dbCpu,
                                    @RequestParam("dbRam") int dbRam,
                                    @RequestParam("dbStorage") int dbStorage,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        String currentLoginUser = userDetails.getUsername();

        Optional<ServerOrder> orderOptional = serverOrderRepository.findById(packageId);
        if (orderOptional.isPresent()) {
            ServerOrder order = orderOptional.get();
            if (order.getUsername().equals(currentLoginUser)) {
                order.setWebCpu(webCpu);
                order.setWebRam(webRam);
                order.setWebStorage(webStorage);
                order.setDbCpu(dbCpu);
                order.setDbRam(dbRam);
                order.setDbStorage(dbStorage);

                serverOrderRepository.save(order);
            }
        }

        return "redirect:/servers/detail/" + packageId;
    }

    /**
     * 6. 서버 영구 삭제 엔드포인트
     */
    @PostMapping("/servers/delete")
    public String deleteServer(@RequestParam("packageId") Long packageId,
                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        String currentLoginUser = userDetails.getUsername();

        Optional<ServerOrder> orderOptional = serverOrderRepository.findById(packageId);
        if (orderOptional.isPresent()) {
            ServerOrder order = orderOptional.get();
            if (order.getUsername().equals(currentLoginUser)) {
                serverOrderRepository.deleteById(packageId);
            }
        }

        return "redirect:/dashboard";
    }
}