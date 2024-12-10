package com.linecorp.line.auth.fido.fido2.common.server;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRpRequest {
    @NotNull
    private String rpId;
}
