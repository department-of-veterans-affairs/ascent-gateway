# README #

This document provides the details of **Ascent Gateway Acceptance test** .

## Acceptance test for Ascent Gateway ##
Acceptance test are created to make sure the core services in ascent gateway are working as expected.

Project uses Java - Maven platform, the REST-Assured jars for core API validations.

## Project Structure ##

src/inttest/gov/va/ascent/features - This is where you will create the cucumber feature files that contain the Feature and Scenarios for the gateway service you are testing.

src/inttest/java/gov/va/ascent/gateway/steps- The implementation steps related to the feature and scenarios mentioned in the cucumber file for the API needs to be created in this location.

src/inttest/java/gov/va/ascent/gateway/runner - Cucumber runner class that contains all feature file entries that needs to be executed at runtime. The annotations provided in the cucumber runner class will assist in bridging the features to step definitions.

src/inttest/resources/logback-test.xml - Logback Console Appender pattern and loggers defined for this project

src/inttest/resources/config/vetservices-ci.properties – CI configuration properties such as URL are specified here.

src/inttest/resources/config/vetservices-stage.properties – STAGE configuration properties such as URL are specified here.

## Execution ##
**Command Line:** Use this command(s) to execute the gateway acceptance test. 

Default Local: mvn -Ddockerfile.skip=true integration-test -Pinttest

Use below sample commands to execute for different environment:

CI: mvn -Ddockerfile.skip=true integration-test -Pinttest -Dtest.env=ci -DX-Vault-Token=<>  -DbaseURL=https://ci.internal.vets-api.gov -Dvault.url=https://vault.internal.vets-api.gov:8200/v1/secret/application

STAGE: mvn -Ddockerfile.skip=true integration-test -Pinttest -Dtest.env=stage -DX-Vault-Token=<>  -DbaseURL=https://stage.internal.vets-api.gov -Dvault.url=https://vault.internal.vets-api.gov:8200/v1/secret/application

The parameter X-Vault-Token is not valid for local environment. It is passed thru pipeline. 



