package com.hosting.controller;

import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.ServerRequestRepository;
import com.hosting.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final ServerRequestRepository serverRequestRepository;
    private final MemberService memberService;

    @PostMapping("/save-and-checkout")
    public String saveAndCheckout(
            @ModelAttribute ServerRequest serverRequest,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Member loginMember = memberService.findByUsername(username);

        serverRequest.setMember(loginMember);
        serverRequest.setStatus("PENDING");

        ServerRequest saved = serverRequestRepository.save(serverRequest);

        return "redirect:/payments/checkout?requestId=" + saved.getRequestId();
    }

    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam("requestId") Long requestId, Model model) {
        ServerRequest request = serverRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다."));

        model.addAttribute("request", request);
        return "payment/checkout";
    }

    @PostMapping("/approve")
    public String approvePayment(@RequestParam("requestId") Long requestId) {
        ServerRequest request = serverRequestRepository.findById(requestId).orElseThrow();
        request.setStatus("PAID");
        serverRequestRepository.save(request);

        return "redirect:/dashboard?payment=success";
    }
}