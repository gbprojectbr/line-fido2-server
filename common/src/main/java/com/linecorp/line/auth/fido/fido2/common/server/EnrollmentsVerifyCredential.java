package com.linecorp.line.auth.fido.fido2.common.server;

import com.linecorp.line.auth.fido.fido2.common.PublicKeyCredentialType;
import com.linecorp.line.auth.fido.fido2.common.extension.AuthenticationExtensionsClientOutputs;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentsVerifyCredential {
    private RequestData data;

    @Data
    public static class RequestData {
        private String enrollmentId;
        private RiskSignals riskSignals;
        private FidoAssertion fidoAssertion;

        @Data
        public static class RiskSignals {
            private String deviceId;
            private Boolean isRootedDevice;
            private Double screenBrightness;
            private Integer elapsedTimeSinceBoot;
            private String osVersion;
            private String userTimeZoneOffset;
            private String language;
            private ScreenDimensions screenDimensions;
            private LocalDate accountTenure;
            private Geolocation geolocation;
            private Boolean isCallInProgress;
            private Boolean isDevModeEnabled;
            private Boolean isMockGPS;
            private Boolean isEmulated;
            private Boolean isMonkeyRunner;
            private Boolean isCharging;
            private String antennaInformation;
            private Boolean isUsbConnected;
            private Integrity integrity;

            @Data
            public static class ScreenDimensions {
                private Integer height;
                private Integer width;
            }

            @Data
            public static class Geolocation {
                private Double latitude;
                private Double longitude;
                private String type;
            }

            @Data
            public static class Integrity {
                private String appRecognitionVerdict;
                private String deviceRecognitionVerdict;
            }
        }

        @Data
        public static class FidoAssertion {
            private String id;
            private String rawId;
            private PublicKeyCredentialType type;
            private ChallengeResponse response;
            private AuthenticationExtensionsClientOutputs clientExtensionResults;

            @Data
            public static class ChallengeResponse {
                private String clientDataJSON;
                private String authenticatorData;
                private String signature;
                private String userHandle;
            }
        }
    }
}