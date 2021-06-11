package com.signicat.demo.sampleapp.inapp.mobile;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.ErrorResponse;

// ==========================================
// Mobile initiated
// ==========================================

// TODO: 10/05/2021 Implement this Class after re-registration is implemented in scid plugin

@RestController("MobileReregisterController")
@RequestMapping("/mobile/re-register")
@EnableAutoConfiguration
public class MobileReregisterController {
    private static final Logger LOG = LoggerFactory.getLogger(MobileReregisterController.class);

    @GetMapping("/start")
    public BaseResponse startReregistration(
            @RequestParam(value = "externalRef") final String extRef,
            @RequestParam(value = "deviceName", required = false) final String devName,
            @RequestParam(value = "deviceId", required = false) final String devId,
            final HttpServletRequest request) {
        LOG.info("PATH: /start");

        return new ErrorResponse("Method not implemented");
    }
}
