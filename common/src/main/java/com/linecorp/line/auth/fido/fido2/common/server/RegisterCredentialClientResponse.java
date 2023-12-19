package com.linecorp.line.auth.fido.fido2.common.server;

import com.linecorp.line.auth.fido.fido2.common.AuthenticatorTransport;
import lombok.Data;

import java.util.List;

@Data
public class RegisterCredentialClientResponse {
    private String clientDataJSON;
    private String attestationObject;
//    private List<AuthenticatorTransport> transports;
}
