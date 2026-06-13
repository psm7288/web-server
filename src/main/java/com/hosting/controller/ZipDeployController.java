package com.hosting.controller;

import com.hosting.service.ZipDeployService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ZipDeployController {

    private final ZipDeployService zipDeployService;

    public ZipDeployController(ZipDeployService zipDeployService) {
        this.zipDeployService = zipDeployService;
    }

    @PostMapping("/servers/detail/{requestId}/deploy")
    public String deployZip(
            @PathVariable Long requestId,
            @RequestParam("zipFile") MultipartFile zipFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            zipDeployService.deployZip(requestId, zipFile);
            redirectAttributes.addFlashAttribute("successMessage", "ZIP 파일 배포가 완료되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/servers/detail/" + requestId;
    }
}
