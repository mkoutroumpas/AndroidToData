# DataAccess

Procedure of creating a SAP HANA MDC instance on SAP Neo (R) Cloud environment and exposing it through a Java web application (Servlet):


1. Go to account.hanatrial.ondemand.com and register for a new trial account. 

2. In the Neo (R) management cockpit (account.hanatrial.ondemand.com/cockpit ), go to the Databases section and configure a new HANA MDC instance. 
Enable Web Access to the newly created database instance. Please note that HANA MDC trial instances have an uptime of 24 hours; you will
have to start up your instance manually after this timespan has elapsed. They are also automatically deleted 1 month after their
creation. 

3. Create your Servlet application, export it as a WAR file and upload it to management . Useful tips:

    a. Servlet deployment descriptor header should look like this:
  
    <?xml version="1.0" encoding="UTF-8"?>
    <web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://java.sun.com/xml/ns/javaee"
      xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
      id="WebApp_ID" version="2.5">
    
    b. When uploading, select Java Web runtime library, Dynamic Web Module v.2.5, Java 1.7 facet and JavaEE 6 Web Profile/JRE7 on the
  Java Application parameterization.
    
    c. Remember to start your HANA MDC database instance and set the appropriate Logger level of your Servlet app in management cockpit.
  
4. Access your newly deployed application through its URL (e.g. https://dataaccessp1942162386trial.hanatrial.ondemand.com/DataAccess ) from any client application, such as a browser or an Android app!
