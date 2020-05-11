Sample MobileID InApp Backend
=====================================

This is a simple sample backend to be used with a merchant's mobile app. Registration and authentication start either on the merchant's website or on the merchant's mobile app. The sample backend server uses the OIDC protocol for communication with Signicat.


Configuration
-------------------------------

-   This project reads the following application configuration file:

        src/main/resources/application.yaml

-   The "oidc" section of this file contains properties that have to be changed prior to execution:
	-   The "redirectUrl" parameter is the callback URL that Signicat's server will use to post results to, once operation (reg or auth) is carried out. 
	    Therefore, this server must be publicly accessible.
	    Also, this redirect_uri has to be configured on Signicat's side, for the particular OIDC client you use.
         

Build and execute
-------------------------------
In order to use this sample code, first build the project:

        mvn clean install

Then, run it:
    
        java -jar target/sample-mobileid-inapp-web-backend.jar
        
If you have not changed the "port" property in the "server" section of the application.yaml file, the server can be accessed at 

        http://localhost:8089
        
