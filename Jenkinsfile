@Library('ascent') _

microservicePipeline {
    imageName = 'ascent/ascent-gateway'

    //Specify string of comma separated upstream projects that will
    //trigger this build if successful
    upstreamProjects = '../ascent-platform/development'

    /*
    Define a mapping of environment variables that will be populated with Vault token values
    from the associated vault token role
    */
    vaultTokens = [
        "VAULT_TOKEN": "ascent-platform"
    ]
    testEnvironment = ['docker-compose.yml', 'docker-compose.override.yml']
    serviceToTest = 'ascent-gateway'
    deployWaitTime = 300
    testVaultTokenRole = "ascent-platform"
    containerPort = 8762

    /*********  Deployment Configuration ***********/
    stackName = "gateway"
    serviceName = "ascent-gateway"

    //Default Deployment Configuration Values
    //composeFiles = ["docker-compose.yml"]
}