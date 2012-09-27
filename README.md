Overview
--------
ChoiceView is a Communications-as-a-Service (CAAS) platform that allows visual information
to be sent from a contact center agent or IVR to mobile users equipped with the ChoiceView app.

Description
-----------
The ChoiceView IVR Web API is a REST-based service that provides IVR systems and telephony services
access to ChoiceView features.

This repository contains source code for a java library wrapper for the Web API (choiceview-webapi-java). The library provides basic methods that can be used to integrate ChoiceView with java-based application servers.  The methods allow you to communicate with ChoiceView servers without having to deal with either XML or JSON.

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
TBD

Building the library
--------------------
The choiceview-webapi-java library is built and maintained as a [Maven](http://maven.apache.org) project. Once that you have Maven installed you can easily build this library by running the following command from the project's root folder:

mvn project

Maven will locate and download all of the build dependencies from public Maven repositories. All of the major Java IDEs (Eclipse, NetBeans, and IntelliJ IDEA) support Maven.

Using the Maven artifact
------------------------
The choiceview-webapi-java is at an early stage of development. We currently don't have a maven repository for hosting maven artifacts, nor is the artifact available from any of the public Maven repositories.  We hope to provide a repository for access soon.

Examples
--------
The unit tests provide examples of how to make ChoiceView API calls with the java library.  There is also an API tester program with a GUI that can be used to send text and urls to a ChoiceView enabled device.

Contact Information
-------------------
If you want more information on ChoiceView, or want access to the ChoiceView Web API, contact us.

[Radish Systems, LLC](http://www.radishsystems.com/support/contact-radish-customer-support/)

-	support@radishsystems.com
-	darryl@radishsystems.com
-	+1.720.440.7560
