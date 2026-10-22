package com.courseshare.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/login",
            "/register",
            "/profile",
            "/resources",
            "/resources/**",
            "/author/**",
            "/admin/**"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}

