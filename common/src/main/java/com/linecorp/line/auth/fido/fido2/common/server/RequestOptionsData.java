package com.linecorp.line.auth.fido.fido2.common.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestOptionsData {
    private String rp;
    private String platform;
}