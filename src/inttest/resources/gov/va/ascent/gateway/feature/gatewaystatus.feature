Feature: Log in to gateway service to check the service is up 

@gatewaystatus
  Scenario Outline: Log in to gateway service to check the status 
      Given I pass the header information for gateway service
      | Pragma       | no-cache        |
      When user makes a request to gateway "<ServiceURL>"
      Then the response code must be for gateway service 200
  Examples: 
      | ServiceURL          |
      |/actuator/health     |