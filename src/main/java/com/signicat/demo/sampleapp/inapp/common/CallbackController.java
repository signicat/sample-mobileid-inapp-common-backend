package com.signicat.demo.sampleapp.inapp.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.ErrorResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SuccessResponse;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;

@RestController("CallbackController")
@RequestMapping("")
@EnableAutoConfiguration
public class CallbackController {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackController.class);

    @Autowired
    private OIDCProperties oidcProperties;

    @Autowired
    private StateCache stateCache;

    @Autowired
    private SessionData sessionData;

    @GetMapping("/consumeOidc")
    public @ResponseBody
    BaseResponse consume(
            @RequestParam(value = "state") final String state,
            final HttpServletRequest request) {

        LOG.info("/consumeOidc message received with state= {}", state);

        final Map<String, String[]> paramsMap = request.getParameterMap();
        LOG.info("paramsMap {}", paramsMap.toString());
        for (final Map.Entry m:paramsMap.entrySet()) {
            LOG.info("key {}", m.getKey());
            for (final String s:(String[])m.getValue()) {
                LOG.info("value {}",  s);
            }
                    }

        if (paramsMap.containsKey("error")) {
            LOG.error(paramsMap.get("error_description")[0]);
            return new ErrorResponse(paramsMap.get("error_description")[0]);
        }

        // validate state/session
        if(state.startsWith(OIDCUtils.INAPP_CHANNEL)){
            // validate inapp session
            LOG.info("validating in-app session... ");
            final String originalState = (String) request.getSession().getAttribute(OIDCUtils.ORIG_STATE);
            if (originalState == null || !originalState.equals(state)) {
                LOG.error("invalid inapp session... ");
                return new ErrorResponse("State empty or mismatch");
            }
        } else {
            // validate web session
            LOG.info("validating web session... ");
            final String stateKey = request.getHeader(WebAppUtils.STATE_KEY);
            stateCache.validateState(stateKey, state);
        }

        if (paramsMap.containsKey("code")) {
            final String code = paramsMap.get("code")[0];
            return processAuthorizationCode(code, state);
        }

        return new ErrorResponse("Fail. Something went wrong!!!");
    }

    private BaseResponse processAuthorizationCode(final String code, final String state) {
        LOG.info("Received code = {} - exec /token request to obtain access_token", code);
        final JSONObject tokenJson = OIDCUtils.getToken(sessionData.getHttpClient(), oidcProperties, code);

        final String errorDesc = "error_description";
        if (tokenJson.containsKey(errorDesc)) {
            LOG.error(errorDesc);
            return new ErrorResponse((String) tokenJson.get(errorDesc));
        }

        final String accessToken = (String) tokenJson.get("access_token");
        LOG.info("Received accessToken = {} - exec /userinfo request", accessToken);

        if(state.startsWith(OIDCUtils.SIGN_CHANNEL)){
            final String signedResult = getSignResult(accessToken);
            return new SuccessResponse(signedResult);
        } else {
            LOG.info("getting userinfo...");
            final JSONObject userInfoJson = getUserInfo(accessToken);
            if (!userInfoJson.isEmpty()) {
                return new SuccessResponse(userInfoJson);
            }
        }

        return new ErrorResponse("Fail. Something went wrong!!!");
    }

    private JSONObject getUserInfo(final String accessToken) {
        final HttpGet httpGet = new HttpGet(oidcProperties.getIssuerId() + "userinfo");
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        try (CloseableHttpResponse httpResponse = sessionData.getHttpClient().execute(httpGet)) {
            final String content = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(content, JSONObject.class);
        } catch (final IOException e) {
            throw new ApplicationException("Fetching userinfo failed: " + e.getMessage());
        }
    }

    private String getSignResult(final String accessToken) {
        final HttpGet httpGet = new HttpGet(oidcProperties.getIssuerId() + "signature");
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_XML.toString());

        try (CloseableHttpResponse httpResponse = sessionData.getHttpClient().execute(httpGet)) {
            final String content = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            return content;
        } catch (final IOException e) {
            throw new ApplicationException("Fetching signature failed: " + e.getMessage());
        }
    }

}
