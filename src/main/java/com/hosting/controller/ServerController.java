package com.hosting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    public String shopPage(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
        return "shop";
    }
}