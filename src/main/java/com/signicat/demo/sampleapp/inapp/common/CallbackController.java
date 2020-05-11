package com.signicat.demo.sampleapp.inapp.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.signicat.demo.sampleapp.inapp.common.beans.BaseResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.ErrorResponse;
import com.signicat.demo.sampleapp.inapp.common.beans.SuccessResponse;
import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.utils.OIDCUtils;
import com.signicat.demo.sampleapp.inapp.common.utils.WebAppUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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
            @RequestParam(value = "state", required = true) final String state,
            final HttpServletRequest request,
            final HttpServletResponse response) {

        LOG.debug("/consumeOidc message received with state= " + state);

        final Map<String, String[]> paramsMap = request.getParameterMap();
        LOG.debug("paramsMap " + paramsMap.toString());

        if (paramsMap.containsKey("error")) {
            LOG.error(paramsMap.get("error_description")[0]);
            return new ErrorResponse(paramsMap.get("error_description")[0]);
        }

        // validate state/session
        if(state.startsWith(OIDCUtils.INAPP_CHANNEL)){
            // validate inapp session
            final String originalState = (String) request.getSession().getAttribute(OIDCUtils.ORIG_STATE);
            if (originalState == null || !originalState.equals(state)) {
                return new ErrorResponse("State empty or mismatch");
            }
        } else {
            // validate web session
            final String stateKey = request.getHeader(WebAppUtils.STATE_KEY);
            stateCache.validateState(stateKey, state);
        }

        if (paramsMap.containsKey("code")) {
            final String code = paramsMap.get("code")[0];
            return processAuthorizationCode(code);
        }

        return new ErrorResponse("Fail. Something went wrong!!!");
    }

    private BaseResponse processAuthorizationCode(String code) {
        final CloseableHttpClient httpClient =
                HttpClientBuilder.create().useSystemProperties().build();

        LOG.debug("Received code =" + code + " - exec /token request to obtain access_token");
        final JSONObject tokenJson = OIDCUtils.getToken(sessionData.getHttpClient(), oidcProperties, code);

        final String errorDesc = "error_description";
        if (tokenJson.containsKey(errorDesc)) {
            LOG.error(errorDesc);
            return new ErrorResponse((String) tokenJson.get(errorDesc));
        }

        final String accessToken = (String) tokenJson.get("access_token");
        LOG.debug("Received accessToken =" + accessToken + " - exec /userinfo request");

        final JSONObject userInfoJson = getUserInfo(accessToken);
        if (!userInfoJson.isEmpty()) {
            return new SuccessResponse(userInfoJson);
        }

        return new ErrorResponse("Fail. Something went wrong!!!");
    }

    private JSONObject getUserInfo(String accessToken) {
        final HttpGet httpGet = new HttpGet(oidcProperties.getIssuerId() + "userinfo");
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        try (CloseableHttpResponse httpResponse = sessionData.getHttpClient().execute(httpGet)) {
            final String content = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
            return new ObjectMapper().readValue(content, JSONObject.class);
        } catch (final IOException e) {
            throw new ApplicationException("Fetching userinfo failed: " + e.getMessage());
        }
    }

}
