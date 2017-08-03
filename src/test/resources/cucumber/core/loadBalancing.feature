###########################################################################################################
# Test Script:
# Use Case ID:
# Objective: To provide system acceptance criteria for load balancing and expected behavior
#
#
###########################################################################################################
Feature: Gateway - Load Balancing

  As a User
  I want to invoke multiple API service end points via Ascent Gateway
  so that all outbound requests to the service instances are load balanced 
  
  Scenario: Load Balancing of service requests
  	Given the user is authorized to invoke service
  	Given the service has multiple instances running
	When I access the service end point for URI starting with /api
	And I access the service end point multiple times
	Then I should see outbound requests being sent to the available instances evenly
	
  Scenario: Load Balancing of service requests when instance goes down
  	Given the user is authorized to invoke service
  	Given the service instance goes offline for S1
	When I access the service end point for URI starting with /api
	And I access the service end point in less than 30 seconds of instance going down 
	And the request is sent to the S1 service instance
	Then I should see 500 error response back from mapped service
	
  Scenario: Load Balancing of service requests when instance goes down
  	Given the user is authorized to invoke service
  	Given the service instance goes offline for S1
	When I access the service end point for URI starting with /api
	And I access the service end point after 30 seconds of instance going down 
	Then I should see 404 error response back from gateway server
	
  Scenario: Load Balancing of service requests when instance comes up
  	Given the user is authorized to invoke service
  	Given the service instance comes back online for S1
	When I access the service end point for URI starting with /api
	And I access the service end point in less than 30 seconds of instance coming up 
	And the request is sent to the S1 service instance
	Then I should see 404 error response back from gateway server
	
  Scenario: Load Balancing of service requests when instance comes up
  	Given the user is authorized to invoke service
  	Given the service instance comes back online for S1
	When I access the service end point for URI starting with /api
	And I access the service end point after 30 seconds of instance coming up
	Then I should see outbound requests being sent to the available instances evenly
	
	

  	
  

    
    

