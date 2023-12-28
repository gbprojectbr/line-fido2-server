/*
 * Copyright 2021 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.line.auth.fido.fido2.rpserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.line.auth.fido.fido2.common.PublicKeyCredentialRpEntity;
import com.linecorp.line.auth.fido.fido2.common.server.*;
import com.linecorp.line.auth.fido.fido2.rpserver.config.FidoServerConfig;
import com.linecorp.line.auth.fido.fido2.rpserver.model.AdapterAuthServerPublicKeyCredential;
import com.linecorp.line.auth.fido.fido2.rpserver.model.AdapterRegServerPublicKeyCredential;
import com.linecorp.line.auth.fido.fido2.rpserver.model.Status;
import com.linecorp.line.auth.fido.fido2.rpserver.model.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

@Slf4j
@RestController
public class AdapterController {
    @Value("${fido2.rp.id}")
    private String rpId;
    @Value("${fido2.rp.origin}")
    private String rpOrigin;
    @Value("${fido2.rp.port}")
    private String rpPort;
    private String regChallengeUri;
    private String regResponseUri;
    private String authChallengeUri;
    private String authResponseUri;

    private String fidoServerHost;
    private String scheme;

    private final RestTemplate restTemplate;
    private final FidoServerConfig fidoServerConfig;

    private final String COOKIE_NAME = "fido2-session-id";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AdapterController(RestTemplate restTemplate, FidoServerConfig fidoServerConfig) {
        this.restTemplate = restTemplate;
        this.fidoServerConfig = fidoServerConfig;
    }

    @PostConstruct
    public void prepareUri() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

        fidoServerHost = fidoServerConfig.getHost();
        scheme = fidoServerConfig.getScheme();
        log.debug("fidoServerHost: {}", fidoServerHost);
        log.debug("scheme: {}", scheme);

        regChallengeUri = uriComponentsBuilder
                .scheme(scheme)
                .host(fidoServerHost)
                .port(fidoServerConfig.getPort())
                .path(fidoServerConfig.getEndpoint().getGetRegChallenge())
                .build().toUriString();

        uriComponentsBuilder = UriComponentsBuilder.newInstance();
        regResponseUri = uriComponentsBuilder
                .scheme(scheme)
                .host(fidoServerHost)
                .port(fidoServerConfig.getPort())
                .path(fidoServerConfig.getEndpoint().getSendRegResponse())
                .build().toUriString();

        uriComponentsBuilder = UriComponentsBuilder.newInstance();
        authChallengeUri = uriComponentsBuilder
                .scheme(scheme)
                .host(fidoServerHost)
                .port(fidoServerConfig.getPort())
                .path(fidoServerConfig.getEndpoint().getGetAuthChallenge())
                .build().toUriString();

        uriComponentsBuilder = UriComponentsBuilder.newInstance();
        authResponseUri = uriComponentsBuilder
                .scheme(scheme)
                .host(fidoServerHost)
                .port(fidoServerConfig.getPort())
                .path(fidoServerConfig.getEndpoint().getSendAuthResponse())
                .build().toUriString();

    }

    // registration
    @PostMapping("/attestation/options")
    public ServerPublicKeyCredentialCreationOptionsResponse getRegistrationChallenge(
            @RequestHeader String host,
            @RequestBody ServerPublicKeyCredentialCreationOptionsRequest optionsRequest,
            HttpServletResponse httpServletResponse) throws JsonProcessingException {

        // set header
        HttpHeaders httpHeaders = new HttpHeaders();

        // set options
        PublicKeyCredentialRpEntity rp = new PublicKeyCredentialRpEntity();
        rp.setName("Test RP");
        // just for test
        rp.setId(rpId);
        ServerPublicKeyCredentialUserEntity user = new ServerPublicKeyCredentialUserEntity();
        user.setName(optionsRequest.getUsername());
        user.setId(createUserId(optionsRequest.getUsername()));
        user.setDisplayName(optionsRequest.getDisplayName());

        RequestOptionsData reqData = RequestOptionsData.builder()
                .rp(rpId)
                .platform("BROWSER")
                .build();

        RegOptionRequest1 regOptionRequest = RegOptionRequest1
                .builder()
                .data(reqData)
                .build();

        HttpEntity<RegOptionRequest1> request = new HttpEntity<>(regOptionRequest, httpHeaders);
        System.out.println("request -------> " + objectMapper.writeValueAsString(request));
        String uri = regChallengeUri.replace("{ENROLLMENT_ID}", optionsRequest.getEnrollmentId());
        System.out.println("URI: " + uri);
        RegOptionResponse1 response = restTemplate.postForObject(uri, request, RegOptionResponse1.class);
        System.out.println("response -------> " + objectMapper.writeValueAsString(response));

        ServerPublicKeyCredentialCreationOptionsResponse serverResponse = ServerPublicKeyCredentialCreationOptionsResponse
                .builder()
                .rp(response.getData().getRp())
                .user(response.getData().getUser())
                .attestation(response.getData().getAttestation())
                .authenticatorSelection(response.getData().getAuthenticatorSelection())
                .challenge(response.getData().getChallenge())
                .excludeCredentials(response.getData().getExcludeCredentials())
                .pubKeyCredParams(response.getData().getPubKeyCredParams())
                .timeout(response.getData().getTimeout())
                .extensions(response.getData().getExtensions())
                .build();

        serverResponse.setStatus(Status.OK);

        return serverResponse;
    }

    @PostMapping("/attestation/result")
    public AdapterServerResponse sendRegistrationResponse(
            @RequestHeader String host,
            @RequestBody AdapterRegServerPublicKeyCredential clientResponse,
            HttpServletRequest httpServletRequest) throws JsonProcessingException {

        AdapterServerResponse serverResponse;

        // get session id
//        Cookie[] cookies = httpServletRequest.getCookies();
//        if (cookies == null || cookies.length == 0) {
//            //error
//            serverResponse = new AdapterServerResponse();
//            serverResponse.setStatus(Status.FAILED);
//            serverResponse.setErrorMessage("Cookie not found");
//            return serverResponse;
//        }

//        String sessionId = null;
//        for (Cookie cookie : cookies) {
//            if (COOKIE_NAME.equals(cookie.getName())) {
//                sessionId = cookie.getValue();
//                break;
//            }
//        }

        // prepare origin
        String scheme = httpServletRequest.getScheme();

        StringBuilder builder = new StringBuilder()
                .append(scheme)
                .append("://")
                .append(rpOrigin);

        if (!StringUtils.isEmpty(rpPort)) {
            builder.append(":")
                    .append(rpPort);
        }

        // set header
        HttpHeaders httpHeaders = new HttpHeaders();

        RegisterCredentialClientResponse clientResponseObj = new RegisterCredentialClientResponse();
        clientResponseObj.setClientDataJSON(clientResponse.getResponse().getClientDataJSON());
        clientResponseObj.setAttestationObject(clientResponse.getResponse().getAttestationObject());
//        clientResponseObj.setTransports(clientResponse.getResponse().getTransports());

        RegisterCredential1Data data = new RegisterCredential1Data();
        data.setId(clientResponse.getId());
        data.setRawId(clientResponse.getRawId());
        data.setClientExtensionResults(clientResponse.getExtensions());
        data.setAuthenticatorAttachment("platform");
        data.setType(clientResponse.getType());
        data.setResponse(clientResponseObj);

        RegisterCredential1 registerCredential = new RegisterCredential1();
        registerCredential.setData(data);

        HttpEntity<RegisterCredential1> request = new HttpEntity<>(registerCredential, httpHeaders);

        System.out.println("request -------> " + objectMapper.writeValueAsString(request));
        String uri = regResponseUri.replace("{ENROLLMENT_ID}", clientResponse.getEnrollmentId());
        System.out.println("URI: " + uri);
        restTemplate.postForObject(uri, request, Void.class);

        serverResponse = new AdapterServerResponse();
        serverResponse.setStatus(Status.OK);
        return serverResponse;
    }

    // authentication
    @PostMapping("/assertion/options")
    public ServerPublicKeyCredentialGetOptionsResponse getAuthenticationChallenge(
            @RequestHeader String host,
            @RequestBody ServerPublicKeyCredentialGetOptionsRequest optionRequest,
            HttpServletResponse httpServletResponse) throws JsonProcessingException {

        // set header
        HttpHeaders httpHeaders = new HttpHeaders();

        EnrollmentsAuthOptionRequest.RequestData authOptionRequestData = EnrollmentsAuthOptionRequest.RequestData
                .builder()
                .rp(rpId)
                .platform("BROWSER")
                .consentId(optionRequest.getConsentId())
                .build();

        EnrollmentsAuthOptionRequest authOptionRequest = EnrollmentsAuthOptionRequest
                .builder()
                .data(authOptionRequestData)
                .build();

        HttpEntity<EnrollmentsAuthOptionRequest> request = new HttpEntity<>(authOptionRequest, httpHeaders);
        System.out.println("request ----> " + objectMapper.writeValueAsString(request));
        String uri = authChallengeUri.replace("{ENROLLMENT_ID}", optionRequest.getEnrollmentId());
        System.out.println("URI: " + uri);
        EnrollmentsAuthOptionResponse response = restTemplate.postForObject(uri, request, EnrollmentsAuthOptionResponse.class);
        System.out.println("response ---> " + objectMapper.writeValueAsString(response));

        ServerPublicKeyCredentialGetOptionsResponse serverResponse;
        serverResponse = ServerPublicKeyCredentialGetOptionsResponse
                .builder()
                .allowCredentials(response.getData().getAllowCredentials())
                .challenge(response.getData().getChallenge())
                .rpId(response.getData().getRpId())
                .timeout(response.getData().getTimeout())
                .userVerification(response.getData().getUserVerification())
                .extensions(response.getData().getExtensions())
                .build();

        // error
//        if (response.getServerResponse().getInternalErrorCode() != 0) {
//            serverResponse.setStatus(Status.FAILED);
//            serverResponse.setErrorMessage(response.getServerResponse().getInternalErrorCodeDescription());
//            return serverResponse;
//        }

        serverResponse.setStatus(Status.OK);

//        httpServletResponse.addCookie(new Cookie(COOKIE_NAME, response.getSessionId()));

        return serverResponse;
    }

    @PostMapping("/assertion/result")
    public AdapterServerResponse sendAuthenticationResponse(
            @RequestHeader String host,
            @RequestBody AdapterAuthServerPublicKeyCredential clientResponse,
            HttpServletRequest httpServletRequest) throws JsonProcessingException {

        AdapterServerResponse serverResponse;

        // get session id
//        Cookie[] cookies = httpServletRequest.getCookies();
//        if (cookies == null || cookies.length == 0) {
//            //error
//            serverResponse = new AdapterServerResponse();
//            serverResponse.setStatus(Status.FAILED);
//            serverResponse.setErrorMessage("Cookie not found");
//            return serverResponse;
//        }

//        String sessionId = null;
//        for (Cookie cookie : cookies) {
//            if (COOKIE_NAME.equals(cookie.getName())) {
//                sessionId = cookie.getValue();
//                break;
//            }
//        }

        // prepare origin
        String scheme = httpServletRequest.getScheme();
        StringBuilder builder = new StringBuilder()
                .append(scheme)
                .append("://")
                .append(rpOrigin);

        if (!StringUtils.isEmpty(rpPort)) {
            builder.append(":")
                    .append(rpPort);
        }

        // set header
        HttpHeaders httpHeaders = new HttpHeaders();

        EnrollmentsVerifyCredential.RequestData.RiskSignals.ScreenDimensions screenDimensions =
                new EnrollmentsVerifyCredential.RequestData.RiskSignals.ScreenDimensions();
        screenDimensions.setHeight(1920);
        screenDimensions.setWidth(1080);

        EnrollmentsVerifyCredential.RequestData.RiskSignals riskSignals = new EnrollmentsVerifyCredential.RequestData.RiskSignals();
        riskSignals.setDeviceId("deviceId");
        riskSignals.setIsRootedDevice(false);
        riskSignals.setScreenBrightness(1.0);
        riskSignals.setElapsedTimeSinceBoot(5000);
        riskSignals.setOsVersion("os version");
        riskSignals.setUserTimeZoneOffset("-03");
        riskSignals.setLanguage("pt");
        riskSignals.setScreenDimensions(screenDimensions);
        riskSignals.setAccountTenure(LocalDate.now());

        byte[] userHandleByteArray = Base64.getDecoder().decode(clientResponse.getResponse().getUserHandle());

        EnrollmentsVerifyCredential.RequestData.FidoAssertion.ChallengeResponse challengeResponse =
                new EnrollmentsVerifyCredential.RequestData.FidoAssertion.ChallengeResponse();

        challengeResponse.setClientDataJSON(clientResponse.getResponse().getClientDataJSON());
        challengeResponse.setAuthenticatorData(clientResponse.getResponse().getAuthenticatorData());
        challengeResponse.setSignature(clientResponse.getResponse().getSignature());
        challengeResponse.setUserHandle(new String(userHandleByteArray, StandardCharsets.UTF_8));


        EnrollmentsVerifyCredential.RequestData.FidoAssertion fidoAssertion =
                new EnrollmentsVerifyCredential.RequestData.FidoAssertion();
        fidoAssertion.setId(clientResponse.getId());
        fidoAssertion.setRawId(clientResponse.getRawId());
        fidoAssertion.setType(clientResponse.getType());
        fidoAssertion.setResponse(challengeResponse);
        fidoAssertion.setClientExtensionResults(clientResponse.getExtensions());

        EnrollmentsVerifyCredential.RequestData verifyCredentialData = new EnrollmentsVerifyCredential.RequestData();
        verifyCredentialData.setEnrollmentId(clientResponse.getEnrollmentId());
        verifyCredentialData.setRiskSignals(riskSignals);
        verifyCredentialData.setFidoAssertion(fidoAssertion);

        EnrollmentsVerifyCredential verifyCredential = new EnrollmentsVerifyCredential();
        verifyCredential.setData(verifyCredentialData);


        HttpEntity<EnrollmentsVerifyCredential> request = new HttpEntity<>(verifyCredential, httpHeaders);
        System.out.println("request ---> " + objectMapper.writeValueAsString(request));
        String uri = authResponseUri
                .replace("{ENROLLMENT_ID}", clientResponse.getEnrollmentId())
                .replace("{CONSENT_ID}", clientResponse.getConsentId());

        System.out.println("URI: " + uri);
        EnrollmentsVerifyCredentialResult response = restTemplate.postForObject(uri, request, EnrollmentsVerifyCredentialResult.class);
        System.out.println("response ---> " + objectMapper.writeValueAsString(response));

        serverResponse = new AdapterServerResponse();
        serverResponse.setStatus(Status.OK);
        return serverResponse;
    }

    private String createUserId(String username) {
        return username;
    }
}
