Overview
--------
ChoiceView is a Communications-as-a-Service (CAAS) platform that allows visual information to be sent from a contact center agent or IVR to mobile users equipped with the ChoiceView app.

Description
-----------
The [ChoiceView REST API](http://www.radishsystems.com/for-developers/for-ivr-developers/) is a REST-based service that provides IVR systems and telephony services access to ChoiceView features. 

This repository contains source code for a java library wrapper for the REST API (choiceview-webapi-java). The library provides basic methods that can be used to integrate ChoiceView with java-based application servers.  The methods allow you to use the ChoiceView API without having to deal with HTTP, XML, or JSON.

You can modify this code to meet your specific requirements. This code demonstrates one way to make REST API calls using publicly available java libraries. You do not have to use this library to connect to our service, see the ChoiceView REST API document for a language-independent description of the ChoiceView REST API. Contact Radish to get a copy of the REST API document or to get technical assistance with the API.

### The ChoiceViewSession class
The library defines a ChoiceViewSession class that represents a ChoiceView session. Each instance of this class represents a data connection between a ChoiceView client (the **Client**) and the ChoiceView application server (the **Switch**).

#### ChoiceViewSession Properties
`int getSessionId()`

`String getCallerId()`

`String getCallId()`

`String getStatus()`

`String getNetworkQuality()`

`String getNetworkType()`

`Map<String, String> getProperties()`
 
These methods return the current state of the ChoiceView session as of the last call to `startSession`, `updateSession`, or `endSession`. A detailed explanation of these properties can be found in the REST API document. The properties _callerId_ and _callId_ are the values you passed to the startSession method. The _status_ property is a string indicating the session state. The _networkQuality_ and _networkType_ property are strings indicating the status of the network connection between the ChoiceView mobile client and the Radish Switch. The _properties_ map contains key/value pairs that provide additonal information about the caller, the mobile device, and the ChoiceView client.

Calling these methods returns cached session information, no API call is made to the Radish switch to get this information. To get the current state of these properties, you must call updateSession before calling these methods.
  
#### ChoiceViewSession Methods
##### Constructor
`ChoiceViewSession(String serverAddress, String username, String password)`

    * serverAddress - The IP address or fully qualified domain name (FQDN) of the Radish Switch.
    * username, password - The credentials needed to connect to the Switch.
     
Radish provides you the FQDN and credentials for a Switch when you purchase a license to use the API. Contact Radish if you want credentials to evaluate the API.
 
##### startSession
`boolean startSession(String callerId, String callId)`

    * callerId - The phone number of the caller connected to the IVR.
    * callid - A string or number used by the IVR to identify the call to the IVR.
    
This method starts the ChoiceView session. The callerId is usually the IVR caller's phone number. It must match the phone number the caller gives to the ChoiceView client web page or application. The callid is optional, it allows the IVR to provide an id value that can be used to identify this specific session in the accounting database and logs.
The IVR application must check the session instance periodically for state changes. The user can end the ChoiceView session at any time, or the session can be interrupted due to mobile network issues. When the session ends, the IVR application should notify the caller that the session has ended and hang up. If the session is interrupted, the IVR application should wait for up to two minutes for the session to resume, then hang up. The Switch and the Client will automatically end suspended sessions after two minutes.

`boolean startSession(String callerId, String callId, String stateChangeUri, String newMessageUri, String notificationType)`

    * callerId - The phone number of the caller connected to the IVR.
    * callid - A string or number used by the IVR to identify the call to the IVR.
    * stateChangeUri - A uri that the API will call whenever the session state changes.
    * newMessageUri - A uri that the API will call whenever new data is received from the client.
    * notificationType - The notification type is either 'basic' or 'CCXML'.

This method allows you to specify urls that the API can call when the ChoiceView session state changes or user input is received from the ChoiceView client (data from a web form or a button press). These urls are commonly referred to as _webhooks_. To use webhooks, the uris must be accessible from the FQDN of the Radish Switch.
If the notificationType is 'CCXML', the uris are treated as access uris defined in Appendix K of the [CCXML Version 1 specification](http://www.w3.org/TR/ccxml/#basichttpio). The session state and any available user data is included in the body of the POST request to the uri. CCXML is a protocol supported by some VoiceXML IVRs.
If the notificationType is 'basic', the uris are treated as a simple webhook, the API will simply make a GET request to the uri when an event occurs. The user can call another method to retrieve the new session status or the available user data.

This method will block until the API returns the status of the session. This won't happen until the caller either calls the url of the ChoiceView web client or clicks the start button in a ChoiceView mobile application. If this doesn't happen in two minutes, the session will time out and the method will return false.

##### endSession
`boolean endSession()`

This method ends the ChoiceView session. The ChoiceView client will show that the session ended, and the API user will no longer be able to interact with the client.

##### updateSession
`boolean updateSession()`

This method gets the current state of the ChoiceView session. The return code indicates if the state was successfully retrieved.

##### sendUrl
`boolean sendUrl(String url)`

    * url - A url to be displayed in the caller's ChoiceView client.
    
This method sends a url to the ChoiceView client. The client loads the url and displays the web page to the caller.
The url should refer to a web page optimized for display on both Android and iOS mobile devices. The web page can contain forms that send data to another url, or pass data back to the API user via the Radish Switch. See the ChoiceView API document for detailed information on building web pages that send data to the Radish Switch.

##### sendText
`boolean sendText(String msg)`

    * msg - A text message to be displayed in the caller's ChoiceView client.
    
This method sends a text message to the ChoiceView client. The text message is displayed in the message area of the ChoiceView client.

The caller cannot respond to text messages sent by the API. You must use web forms to have a two way text conversation with a caller. Contact Radish for help in designing these types of interactions.

##### getControlMessage
`Map<String, String> getControlMessage()`

This method retrieves the latest form data (including button clicks) generated by a web page displayed in the ChoiceView client. When the caller submits a form or clicks a button on the displayed web page, the API will call the newMessageUri specified when `startSession` was called. If you did not set newMessageUri to a valid uri, you can periodically call `getControlMessage` to check for form data from the caller. The method returns null if no data is available.
The method will continue to return the last form data received until you call `clearControlMessage`. After you call `clearControlMessage`, `getControlMessage` will return null until the caller submits another form or clicks another button on the web page.
The web page forms and controls must be configured to send data to the API when submitted or clicked. See the ChoiceView API document for details on setting up web page controls and forms, and contact Radish if you need technical assistance on setting this up.

##### clearControlMessage
`boolean clearControlMessage()`

This method clears the latest form data returned by `getControlMessage`.

##### updateProperties
`Map<String, String> updateProperties()`

This method returns the latest set of properties for the ChoiceView session.  We often refer to the set of properties as the _Payload_. A property is a key/value pair associated with the ChoiceView session. The properties are logged, but do not persist between sessions. The ChoiceView client will add several properties to the payload when the session starts.  The properties that are added depend on the type of client (web or mobile app), the environment that the client is running in (desktop, Android device, or iOS device), and the information the user of the client has chosen to disclose.  Do not assume that any particular property will be available in all sessions.

When this method is called, it will make an API call to get the latest payload from the Radish Switch. If you want to access the cached payload from the last session update, call `getProperties`.

##### addProperties
`boolean addProperties(Map<String, String> properties)`

    * properties - a map of properties to add to the session payload
    
`boolean addProperty(String name, String value)`

    * name - the name (key) of the property
    * value - the value of the property
    
These methods add one or more properties to the payload. 

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

Sample Programs
---------------
A sample console program is provided that shows how to call the methods in the java library. To use the ApiTester program, you must have a computer, smartphone, or tablet with access to the Internet. If the device cannot make a phone call, you will need the phone number of a landline, mobile phone, or telephony app that can make a call. VOIP calls are not supported at this time, your telephony device must have a traditional phone number, not an email address.

Before you can run the ApiTester program, you must contact Radish and get API credentials. The credentials will be a username and password.  You can enter the credentials from the command line when you start the ApiTester, or you can create two environment variables for the credentials, CHOICEVIEW_USERNAME and CHOICEVIEW_PASSWORD. If these environment variables are set, you do not need to specify them on the command line.

To start the ApiTester program, compile and execute the ApiTester class. If you have maven installed, you can open a command window, cd to the samples/ApiTester directory, then type these commands:

`mvn clean install assembly:single`

`java -jar target/choiceview-sample-apitester-1.1.0-SNAPSHOT-jar-with-dependencies.jar -u [username] -p [password]`

You will be prompted for the caller id and call id to use to start a ChoiceView session with the mobile device. The first value entered should be the mobile phone number, the ChoiceView server uses this value as the caller id. The second value entered is the call id. This value is optional.

After the caller id and call id is entered, you should see a line similar to:

 _Starting session for ..._

 To start the session, you use the device web browser to load the ChoiceView Web Client at http://cvnet2.radishsystems.com/start.html. The tester will then display the current status of the ChoiceView session, and prompt for additional commands. The command __URL__, followed by a valid url, will cause the ChoiceView client on the mobile device to display the url. The command __PROPERTIES__ will display the session properties as defined by the mobile device.  To end the session, type __QUIT__.

 The intent of the java wrapper and the sample programs is to demonstrate some techniques for making REST API calls and to showcase some commonly availble open source libraries that can be used for this purpose. Contact us if you have problems or need help in using or modifying the library or sample programs.

Contact Information
-------------------
If you want more information on ChoiceView, or want access to the ChoiceView REST API, contact us.

[Radish Systems, LLC](http://www.radishsystems.com/support/contact-radish-customer-support/)

-	support@radishsystems.com
-	darryl@radishsystems.com
-	+1.720.440.7560
