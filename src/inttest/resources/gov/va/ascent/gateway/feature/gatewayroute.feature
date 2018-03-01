Feature: Log in to gateway service to check the gateway route 

@gatewayroute
  Scenario Outline: Log in to gateway service to check the gateway route 
      Given I pass the header information for gateway route service
      | Pragma       | no-cache        |
      When user makes a request to gateway route "<ServiceURL>"
      And the response code must be for route service 200
      Then assert the "<property>" refdata route "<values>" 
  Examples: 
      | ServiceURL       |  property         |      values                   |
      |/actuator/routes  |  '/api/refdata/**'| vetservices-refdata           |