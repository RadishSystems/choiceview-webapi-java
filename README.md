Overview
--------
ChoiceView is a Communications-as-a-Service (CAAS) platform that allows visual information to be sent from a contact center agent or IVR to mobile users equipped with the ChoiceView app.

Description
-----------
The [ChoiceView REST API](http://www.radishsystems.com/for-developers/for-ivr-developers/) is a REST-based service that provides IVR systems and telephony services access to ChoiceView features. 

This repository contains source code for a java library wrapper for the REST API (choiceview-webapi-java). The library provides basic methods that can be used to integrate ChoiceView with java-based application servers.  The methods allow you to communicate with ChoiceView servers without having to deal with HTTP, XML, or JSON.

Dependencies
------------
You can install this library in any Java application or application server. The library depends primarily on the [Jackson json processor](http://wiki.fasterxml.com/JacksonHome) and the [HttpClient](http://hc.apache.org/httpcomponents-client-ga/index.html) component from [Apache HttpComponents](http://hc.apache.org). The following listing shows all the libraries that need to be present in an application that uses the choiceview-webapi-java library:

	* hamcrest-core-1.1.jar
	* httpclient-4.2.1.jar
	* httpcore-4.2.1.jar
	* commons-logging-1.1.1.jar
	* commons-codec-1.6.jar
	* httpclient-4.2.1-tests.jar
	* jackson-mapper-asl-1.9.9.jar
	* jackson-core-asl-1.9.9.jar

LICENSE
-------
[MIT License](https://github.com/radishsystems/choiceview-webapi-java/blob/master/LICENSE)

Building the library
--------------------
The _choiceview-webapi-java_ library is built and maintained as a [Maven](http://maven.apache.org) project. Once you have Maven installed you can easily build this library by running the following command from the project's root folder:

mvn project

Maven will locate and download all of the build dependencies from public Maven repositories. All of the major Java IDEs (Eclipse, NetBeans, and IntelliJ IDEA) support Maven.

Using the Maven artifact
------------------------
The _choiceview-webapi-java_ is at an early stage of development. We currently don't have a maven repository for hosting maven artifacts, nor is the artifact available from any of the public Maven repositories.  We hope to provide a repository for access soon.

Sample Programs
---------------
A sample console program is provided that shows how to call the methods in the java library.  To use the ApiTester program, you must have a mobile device with the latest ChoiceView client installed.  You should know the phone number of the mobile device, or the phone number that the ChoiceView client is configured to use.  The client must be configured to use the ChoiceView test server.  On iDevices, press Settings, then Advanced, then change the server field to _cvnet2.radishsystems.com_. On Droids, press the menu button, then Settings, then scroll down to the server field and change it to _cvnet2.radishsystems.com_.

To start the ApiTester program, compile and execute the ApiTester class.  You will be prompted for the caller id and call id to use to start a ChoiceView session with the mobile device.  The first value entered should be the mobile phone number, the ChoiceView server uses this value as the caller id.  The second value entered is the call id. This value is optional. The call id is useful for identifying a specific ChoiceView session out of multiple sessions with the same Caller ID.

After the caller id and call id is entered, you should see a line similar to:

 _Starting session for ..._

 To start the session, you must open the ChoiceView client on your mobile device and press the start button.  The tester will then display the current status of the ChoiceView session, and prompt for additional commands.  The command __URL__, followed by a valid url, will cause the ChoiceView client on the mobile device to display the url.  The command __PROPERTIES__ will display the session properties as defined by the mobile device.  To end the session, type __QUIT__.

 The intent of the java wrapper and the sample programs is to demonstrate some techniques for making REST API calls and to showcase some commonly availble open source libraries that can be used for this purpose. Contact us if you have problems or need help in using or modifying the library or sample programs.

Contact Information
-------------------
If you want more information on ChoiceView, or want access to the ChoiceView REST API, contact us.

[Radish Systems, LLC](http://www.radishsystems.com/support/contact-radish-customer-support/)

-	support@radishsystems.com
-	darryl@radishsystems.com
-	+1.720.440.7560
