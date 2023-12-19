package com.linecorp.line.auth.fido.fido2.common.server;

import com.linecorp.line.auth.fido.fido2.common.PublicKeyCredentialType;
import com.linecorp.line.auth.fido.fido2.common.TokenBinding;
import com.linecorp.line.auth.fido.fido2.common.extension.AuthenticationExtensionsClientOutputs;
import lombok.Data;

import java.util.Map;

@Data
public class RegisterCredential1Data {
    private String id;
    private String rawId;
    private RegisterCredentialClientResponse response;
    private String authenticatorAttachment;
    private PublicKeyCredentialType type;
    private AuthenticationExtensionsClientOutputs clientExtensionResults;
}
