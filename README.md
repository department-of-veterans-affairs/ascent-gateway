# README #

This page documents all the steps that are necessary to get the **Ascent Gateway** service up and running locally and other environments.

## What is this repository for? ##

This is a repository for the ascent gateway service that routes for all the service requests.  

## Core Services Overview ##

**ascent-discovery: Discovery Service**

Spring Cloud Netflix Eureka Discovery Service. REST-based service discovery and registration for fail over and load-balancing

**ascent-config: Cloud Config Service**

Spring Cloud Config to centralize external configuration management, backed by Git. See [README](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Config) for additional details

**ascent-gateway: API Gateway**

Spring Cloud Zuul Gateway Service. It provides Dynamic routing, monitoring, resiliency, security, and more

**ascent-dashboard: Dashboard(s)**

Demo of various dashboards such as Hystrix which is a provided dashboard, Turbine to monitor a single server or a cluster of servers aggregated, custom consolidated swagger dashboard and Monitoring Dashboard (Spring Boot Actuator URLs).  Other dashboards, if we decided to tinker, can go here so we don't need to deploy 50 applications locally to test out basic dashboards.

**Service Application Ports**
* Discovery - 8761
* Cloud Config - 8760
* Gateway - 8762
* Misc. Dashboard(s) - 8763

## How do I get set up? ##

* Ensure you have Maven, JDK8, GIT and optionally Docker installed. 
* Generate a SSH key with passphrase to connect to GitHub as we have 2FA enabled. Click [here](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Quick-Start-Guide#generating-new-ssh-key-with-passphrase-to-connect-to-github) for steps
* The recommended IDE is Spring Tool Suite as it plays most nicely with Spring Boot (obviously).  However you are free to choose.

  To clone and run this repository you'll need Git installed on your computer. 
* From your command line: * git clone https://github.com/department-of-veterans-affairs/ascent.git *
* You could also clone the repository from IDE. Clone repository with URL * https://github.com/department-of-veterans-affairs/ascent.git*
* Change directory to "ascent"
* If Docker is RUNNING, run 'mvn clean install' from the reactor pom to build the project which will create the docker images. 
* If Docker is OFFLINE / UNAVAILABLE, run 'mvn clean install -DskipDockerBuild' from the reactor pom to build the project which will create the docker images. 
* Deployment instructions...

  See below for information on the various techniques.  Currently there are 2 documented ways to deploy locally...
  
  (1) From your IDE you can deploy an "*integrated environment*" that includes all components
  
  (2) You can leverage docker to automatically stand up the integrated environment for demo purposes
  
## Deployment Details ##
  
### (1) IDE Deploy of "*integrated environment*" ###
* Assuming you are using Spring Tool Suite as suggested
* Ensure you've imported the projects in the IDE

**Configure Projects**
* In the "Boot Dashboard" within Spring Tool Suite, highlight ascent-config project and click the "*Open Config*" button
* On the "*Spring Boot*" tab, key in "*local-int*" as the Profile
* On the "*Arguments*" tab add the 1 -D params for your git passphrase and "*Apply/Close*" the window
  
  i.e. *-Dgit.passphrase=<YOUR_GIT_PASSPHRASE>* 
  	
  	*NOTE: GIT CONFIG REPO IS MFA (2FA) ENABLED + SECURED USING SSH KEY WITH PASSPHRASE*
* In the "Boot Dashboard" within Spring Tool Suite, highlight ascent-gateway project and click the "*Open Config*" button
* On the "*Spring Boot*" tab key in "*local-int*" as the profile and "*Apply/Close*" the window
* In the "Boot Dashboard" within Spring Tool Suite, highlight ascent-dashboard project and click the "*Open Config*" button
* On the "*Spring Boot*" tab key in "*local-int*" as the profile and "*Apply/Close*" the window

**Start Projects**
* In the "Boot Dashboard" within Spring Tool Suite, highlight ascent-discovery project and click the "*(Re)start*" button
* In the "Boot Dashboard" within Spring Tool Suite, highlight ascent-config project and click the "*(Re)start*" button
    * WAIT FOR THE 2 PREVIOUS APPS TO START
* In the "Boot Dashboard" within Spring Tool Suite, highlight ascent-gateway project and click the "*(Re)start*" button
* In the "Boot Dashboard" within Spring Tool Suite, highlight ascent-dashboard project and click the "*(Re)start*" button
* URLs for testing/using this deployment approach
  
  [Discovery:Eureka](http://localhost:8761)
   
  [Gateway:Zuul Routes](http://localhost:8762/routes)
  
  [Dashboard](http://localhost:8763)  
   
  *Note it could take a bit (maybe a minute) for Gateway (Zuul) and the service to register with Discovery (Eureka) and then finally for Gateway to obtain the routes to the service.  Be patient while the apps that just came online register and are discovered.* 
  
  *Note there are other URLs, such as all the actuator URLs.  Listed here are the basic minimum URLs.*
   
### (2) Local Docker Deployment ###
* Run 'mvn clean install' from the reactor pom to build the project which will create the docker images.
* Set the env variables that'll be needed by the containers...
  
  *export GIT_PASSPHRASE=<YOUR_GIT_PASSPHRASE>*
  
  *All other environment variables are set via .env file under the parent project directory*

* Start the containers by running the script "*start-all.sh**"
* URLs for testing/using this deployment approach
   
  [Discovery:Eureka](http://localhost:8761)
  
  [Gateway:Zuul Routes](http://localhost:8762/routes)
  
  [Dashboard](http://localhost:8763)
   
  *Note there are other URLs, such as all the actuator URLs.  Listed here are the basic minimum URLs.*
   
  * When done, recommend running "**./stop-all.sh**" to stop and remove the containers, networks, images and volumes so you don't always run at scale.*
  
### (3) Cloud Deployment ###

*We are going to deploy on to GovCloud platform. Stay tuned for additional updates.*
  
## Service Patterns ##

* [Ascent Config: Centralized Repository](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Config)
* [Ascent Discovery: Service discovery and registration for fail over and load-balancing](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Discovery)
* [Ascent Gateway: Dynamic routing, Monitoring, Resiliency and Security](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Gateway)
* [Ascent Hystrix Pattern: Circuit Breaker and Monitoring Dashboard](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Hystrix-Pattern)
* [Ascent Dashboard: Dashboards including Hystrix, Turbine and swagger dashboard](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Dashboard)