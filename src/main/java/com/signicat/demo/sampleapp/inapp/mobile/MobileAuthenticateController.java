package com.signicat.demo.sampleapp.inapp.mobile;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.signicat.demo.sampleapp.inapp.common.OIDCProperties;
import com.signicat.demo.sampleapp.inapp.common.beans.AuthenticationData;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SuccessResponse;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;

@RestController("MobileAuthenticateController")
@RequestMapping("/mobile/authenticate")
@EnableAutoConfiguration
public class MobileAuthenticateController {

    private static final Logger LOG = LoggerFactory.getLogger(MobileAuthenticateController.class);

    @Autowired
    protected OIDCProperties oidcProperties;

    @Value("${server.servlet.session.timeout}")
    protected String            sessionTimeout;

    @GetMapping("/start")
    public @ResponseBody
    BaseResponse startAuthentication(
                    @RequestParam(value = "externalRef") final String extRef,
                    @RequestParam(value = "deviceId") final String devId,
                    final HttpServletRequest request) {
        LOG.debug("/start message received");

        final AuthenticationData authenticationData = prepareAuthenticationData(extRef, devId);
        final String authorizeUrl = OIDCUtils.getAuthorizeUri(authenticationData);
        LOG.info("AUTHORIZE URL: {}", authorizeUrl);

        // setting state in the session
        request.getSession().setAttribute(OIDCUtils.ORIG_STATE, authenticationData.getState());

        return new SuccessResponse(authorizeUrl);
    }

    private AuthenticationData prepareAuthenticationData(final String extRef, final String devId) {
        final String state = OIDCUtils.createState(OIDCUtils.INAPP_CHANNEL + oidcProperties.getAcrValues() + oidcProperties.getAuthMethod());

        return new AuthenticationData(oidcProperties.getOidcBase() + OIDCUtils.AUTHORIZE_RESPONSE_TYPE_CODE, oidcProperties.getClientId(),
                oidcProperties.getScope(),
                oidcProperties.getRedirectUri(),
                oidcProperties.getAcrValues() + oidcProperties.getAuthMethod(), state, extRef, devId);
    }
}
