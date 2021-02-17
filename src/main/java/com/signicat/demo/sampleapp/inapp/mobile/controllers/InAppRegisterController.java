package com.signicat.demo.sampleapp.inapp.mobile.controllers;

import com.signicat.demo.sampleapp.inapp.common.beans.RegistrationData;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SuccessResponse;
import com.signicat.demo.sampleapp.inapp.common.utils.ControllersUtil;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.demo.sampleapp.inapp.common.wsclient.beans.ScidRequest;
import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import com.signicat.generated.scid.CreateArtifactResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@RestController("InAppRegisterController")
@RequestMapping("/mobile/register")
@EnableAutoConfiguration
public class InAppRegisterController {
    private static final Logger LOG = LoggerFactory.getLogger(InAppRegisterController.class);

    @Autowired
    private OIDCProperties oidcProperties;

    @Autowired
    private ScidWsClient        scidWsClient;

    @Value("${server.servlet.session.timeout}")
    protected String            sessionTimeout;

    @GetMapping("/start")
    public @ResponseBody BaseResponse startRegistration(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            @RequestParam(value = "deviceName", required = true) final String devName,
            final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /start ");

        final RegistrationData registrationData = prepareRegistrationData(extRef, devName);
        final String authorizeUrl = OIDCUtils.getAuthorizeUri(registrationData);
        LOG.info("AUTHORIZE URL:" + authorizeUrl);

        // setting state in the session
        request.getSession().setAttribute(OIDCUtils.ORIG_STATE, registrationData.getState());

        return new SuccessResponse(authorizeUrl);
    }

    @GetMapping("/isDeviceActivated")
    public @ResponseBody BaseResponse isDeviceActivated(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            @RequestParam(value = "deviceName", required = true) final String devName,
            final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: /isDeviceActivated");
        return new SuccessResponse(ControllersUtil.isDeviceAlreadyActivated(scidWsClient, extRef, devName));
    }

    @GetMapping("/deleteDevice")
    public @ResponseBody BaseResponse deleteDevice(
            @RequestParam(value = "externalRef", required = true) final String extRef,
            @RequestParam(value = "deviceName", required = true) final String devName,
            final HttpServletRequest request, final HttpServletResponse response) {
        LOG.info("PATH: deleteDevice");
        if (scidWsClient.accountExists(extRef)) {
            // delete device from an account
            scidWsClient.deleteDevice(extRef, devName);
            return new SuccessResponse(true);
        }
        return new SuccessResponse(false);
    }

    private RegistrationData prepareRegistrationData(final String extRef, final String devName) {
        // --- Generate Artifact
        final String artifact = createArtifact(extRef);

        final String state = OIDCUtils.createState( OIDCUtils.INAPP_CHANNEL + oidcProperties.getAcrValues() + oidcProperties.getRegMethod());

        return new RegistrationData(oidcProperties.getOidcBase() + OIDCUtils.AUTHORIZE_RESPONSE_TYPE_CODE, oidcProperties.getClientId(),
                oidcProperties.getScope(),
                oidcProperties.getRedirectUri(),
                oidcProperties.getAcrValues() + oidcProperties.getRegMethod(), state, extRef, devName, artifact);
    }

    private String createArtifact(final String extRef) {
        // --- Account creation is skipped if account already exits
        if (!scidWsClient.accountExists(extRef)) {
            final ScidRequest scidRequest = new ScidRequest(extRef, new ArrayList<>());
            scidWsClient.createAccount(scidRequest);
        }

        // --- Create artifact
        final CreateArtifactResponse artifactResult = (CreateArtifactResponse) scidWsClient.createArtifact(extRef);
        return artifactResult.getArtifact();
    }

}
