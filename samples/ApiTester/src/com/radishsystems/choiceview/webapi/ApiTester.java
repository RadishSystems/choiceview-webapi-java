package com.radishsystems.choiceview.webapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class ApiTester {
	final static String ServerAddress = "cvnet2.radishsystems.com";
	final static String Username = "demo";
	final static String Password = "radisH1!";

	final static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

	static void ShowSession(ChoiceViewSession session) {
		if(session != null) {
			System.out.printf("Session Information\nStatus: %s\n",
					session.getStatus());
			System.out.printf("SessionID: %d, CallID: %s, CallerID: %s\n",
					session.getSessionId(),
					session.getCallId() == null ? "<null>" : session.getCallId(),
					session.getCallerId());
			System.out.printf("Network type: %s, Network quality: %s\n",
					session.getNetworkType(), session.getNetworkQuality());
		}
	}
	static String waitForCommand() {
		String command = null;
		
		System.out.println("\nAvailable Commands:");
		System.out.println("URL <url to send>");
		System.out.println("TEXT <text to send>");
		System.out.println("PROPERTIES");
		System.out.println("TRANSFER <account to transfer to>");
		System.out.println("QUIT");
		System.out.print("\nEnter a command\n> ");
		
		try {
			command = input.readLine();
		} catch (IOException e) {
			System.err.println("Exception caught while waitng for command:");
			e.printStackTrace();
		}
		
		return command;
	}

	public static void main(String [] args) {
		ChoiceViewSession cvSession = null;
		try {
			cvSession = new ChoiceViewSession(ServerAddress, Username, Password);
			if(cvSession != null) {
				String command;
				String callerId, callId;
				if(args == null || args.length == 0) {
					System.out.println("Enter caller id and call id...");
					command = input.readLine();
					if(command == null || command.length() == 0) {
						callerId = null;
						callId = null;
					} else {
						Scanner params = new Scanner(command);
						callerId = params.hasNext() ? params.next() : null;
						callId = (callerId != null && params.hasNext()) ?
								params.next() : null;
					}
				} else {
					callerId = args[0];
					callId = args.length > 1 ? args[1] : null;
				}
				if(callerId == null) {
					System.err.println("No Caller Id specified - Goodbye!");
					return;
				}
				
				System.out.println("Starting session for " + callerId + "...");
				if(!cvSession.startSession(callerId,  callId)) {
					System.err.println("Cannot start session for " + callerId + " - Goodbye!");
					return;
				}
				System.out.println("Session started.");
				do {
					ShowSession(cvSession);
					command = waitForCommand();
					Scanner params = new Scanner(command);
					String param0 = params.hasNext() ? params.next() : null;
					
					if(param0 == null || param0.length() == 0) continue;

					if(param0.equalsIgnoreCase("QUIT")) {
						if(!cvSession.endSession()) {
							System.err.println("Cannot end session!");
						}
					}
					else if(param0.equalsIgnoreCase("PROPERTIES")) {
						Map<String, String> properties = cvSession.getProperties();
						if(properties == null) {
							System.err.println("Cannot get session properties!");
						} else {
							System.out.println("Session Properties:");
							for(Entry<String, String> property : properties.entrySet()) {
								System.out.println(property.getKey() + ": " + property.getValue());
							}
							System.out.println();
						}
					}
					else if(param0.equalsIgnoreCase("URL")) {
						String param1 = params.hasNext() ? params.next() : null;
						if(param1 != null && param1.length() > 0) {
							try {
								URI url = new URI(param1.trim());
								if(!cvSession.sendUrl(url.toURL().toString())) {
									System.err.println("Cannot send url!");
								}
							}
							catch(URISyntaxException e) {
								System.err.println("Bad uri specified!");
							}
							catch(MalformedURLException e) {
								System.err.println("Bad url specified!");
							}
						} else {
							System.err.println("No url specified!");
						}
					}
					else if(param0.equalsIgnoreCase("TEXT")) {
						String param1 = params.hasNext() ? params.nextLine() : null;
						if(param1 != null && param1.length() > 0) {
							if(!cvSession.sendText(param1.trim())) {
								System.err.println("Cannot send text message!");
							}
						} else {
							System.err.println("No text specified!");
						}
					}
					else if(param0.equalsIgnoreCase("TRANSFER")) {
						String param1 = params.hasNext() ? params.nextLine() : null;
						if(param1 != null && param1.length() > 0) {
							if(cvSession.transferSession(param1.trim())) {
								System.out.println("Session transferred to" + param1 + " account.");
							} else {
								System.err.println("Cannot transfer session!");
							}
						} else {
							System.err.println("No text specified!");
						}
					}
				} while(cvSession.updateSession() &&
						!cvSession.getStatus().equals("disconnected"));
			}
		} catch (Exception e) {
			System.err.println("Application terminated due to exception:");
			e.printStackTrace();
		} finally {
			if(cvSession != null && !cvSession.getStatus().equals("disconnected")) {
				try { cvSession.endSession(); }
				catch(IOException te) { System.err.println(te.getMessage()); }
			}
		}
		
		System.out.println("Goodbye!");
	}
}
