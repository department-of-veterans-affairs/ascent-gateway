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

src/inttest/resources/config/vetservices-dev.properties – DEV configuration properties such as URL are specified here.

src/inttest/resources/config/vetservices-stage.properties – STAGE configuration properties such as URL are specified here.

## Execution ##
**Command Line:** Use this command(s) to execute the gateway acceptance test. 

Default Local: mvn -Ddockerfile.skip=true integration-test -Pinttest

Use below sample commands to execute for different environment:

DEV: mvn -Ddockerfile.skip=true integration-test -Pinttest -Dtest.env=dev -DX-Vault-Token=<> -DbaseURL=https://dev.internal.vetservices.gov -Dvault.url.domain=https://dev-vault.internal.vetservices.gov/

STAGE: mvn -Ddockerfile.skip=true integration-test -Pinttest -Dtest.env=stage -DX-Vault-Token=<> -DbaseURL=https://staging.internal.vetservices.gov -Dvault.url.domain=hhttps://staging-vault.internal.vetservices.gov/

The parameter X-Vault-Token is not valid for local environment. It is passed thru pipeline. 



