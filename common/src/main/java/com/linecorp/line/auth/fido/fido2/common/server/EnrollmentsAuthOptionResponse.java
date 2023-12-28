package com.linecorp.line.auth.fido.fido2.common.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.linecorp.line.auth.fido.fido2.common.UserVerificationRequirement;
import com.linecorp.line.auth.fido.fido2.common.extension.AuthenticationExtensionsClientInputs;
import lombok.*;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
@ToString
public class EnrollmentsAuthOptionResponse {

    private ResponseData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(NON_NULL)
    @ToString
    public static class ResponseData {
        private String challenge;
        private long timeout;
        private String rpId;
        private List<ServerPublicKeyCredentialDescriptor> allowCredentials;
        private UserVerificationRequirement userVerification;
        // extension
        private AuthenticationExtensionsClientInputs extensions;
    }
}
