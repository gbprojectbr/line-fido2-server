// If is a merge to main.
if (env.BRANCH_NAME == "main") {
    ciKubernetesDeploySkipSonar {
        serviceNamespace = "openbanking"
        jobName = "pp-ms-line-fido2-server"
        betaFeatures = [
            obk: true,
            syncHMLWithPRD: false
        ]
        cacheVolumes = [
            'Dockerfile-tests': [
                'gradle': '/home/gradle/.gradle'
            ],
            'Dockerfile': [
                'gradle': '/home/gradle/.gradle'
            ]
        ]
    }
}

if (env.BRANCH_NAME == "hml") {
    ciKubernetesDeploySkipSonar {
        serviceNamespace = "openbanking"
        jobName = "pp-ms-line-fido2-server"
        betaFeatures = [
            obk: true
        ]
        cacheVolumes = [
            'Dockerfile-tests': [
                'gradle': '/home/gradle/.gradle'
            ],
            'Dockerfile': [
                'gradle': '/home/gradle/.gradle'
            ]
        ]
    }
}

// If is a pull request.
if (env.CHANGE_ID) {
    ciRunTests {
        serviceNamespace = "openbanking"
        jobName = "pp-ms-line-fido2-server"
        cacheVolumes = [
            'Dockerfile-tests': [
                'gradle': '/home/gradle/.gradle'
            ]
        ]
    }
}