package com.linecorp.line.auth.fido.fido2.server.controller;

import com.linecorp.line.auth.fido.fido2.common.PublicKeyCredentialRpEntity;
import com.linecorp.line.auth.fido.fido2.common.server.RegisterRpRequest;
import com.linecorp.line.auth.fido.fido2.server.service.RpService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RpController {

    private final RpService rpService;

    @Autowired
    public RpController(RpService rpService) {
        this.rpService = rpService;
    }

    @PostMapping(path = "/fido2/reg/rp")
    public void registerRp(@Valid @RequestBody RegisterRpRequest registerRpRequest) {
        rpService.registerRp(registerRpRequest.getRpId());
    }

    @GetMapping(path = "/fido2/rp/{rpId}")
    public PublicKeyCredentialRpEntity getRpById(@PathVariable String rpId) {
        return rpService.get(rpId);
    }
}
