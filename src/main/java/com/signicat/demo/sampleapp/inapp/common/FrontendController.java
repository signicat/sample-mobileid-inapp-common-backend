package com.signicat.demo.sampleapp.inapp.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {
    @GetMapping(value = {"/registration", "/authentication", "/consent-sign"})
    public String entry() {
        return "forward:/index.html";
    }
}
