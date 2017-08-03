###########################################################################################################
# Test Script:
# Use Case ID:
# Objective: To provide system acceptance criteria for gateway routing and expected behavior
#
#
###########################################################################################################
Feature: Gateway - Routing

  As a User
  I want to invoke multiple API service end points via Ascent Gateway (Same Base URL)
  so that the same host and port could be used for all the service calls
  
  Scenario: Routing of requests for API end points
  	Given the user is authorized to invoke service
	When I access the service end point for URI starting with /api 
	Then I should get back a valid response from the mapped service
	
  Scenario: Routing of requests for Non-Existent service
  	Given the user is authorized to invoke service
	When I access the service end point that doesn't exist 
	Then I should be get back 404 error response from the gateway server

  Scenario: Routing of requests for offline service
  	Given the user is authorized to invoke service
	When I access the service end point which has no active instances
	Then I should be get back 404 error response from the gateway server
	

  	
  

    
    

