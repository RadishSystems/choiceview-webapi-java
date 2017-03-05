package com.radishsystems.choiceview.webapi;

import org.apache.commons.cli.*;

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
	private final static String ServerAddress = "cvnet2.radishsystems.com";
	private final static String Username = System.getenv("CHOICEVIEW_USERNAME");
	private final static String Password = System.getenv("CHOICEVIEW_PASSWORD");

	private final static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

	private static void ShowSession(ChoiceViewSession session) {
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
	private static String waitForCommand() {
		String command = null;
		
		System.out.println("\nAvailable Commands:");
		System.out.println("URL <url to send>");
		System.out.println("TEXT <text to send>");
		System.out.println("PROPERTIES");
		System.out.println("CONTROLMSG (must send url of web page with html controls first)");
		System.out.println("CLEARMSG (clears any received control message)");
		System.out.println("ADD <property key> <property value>");
		System.out.println("TRANSFER <account to transfer to>");
		System.out.println("QUIT");
		System.out.print("\nEnter a command\n> ");
		
		try {
			command = input.readLine();
		} catch (IOException e) {
			System.err.println("Exception caught while waiting for command:");
			e.printStackTrace();
		}
		
		return command;
	}

	public static void main(String [] args) {
		ChoiceViewSession cvSession = null;
		String user_param = Username;
		String password_param = Password;
        String callerId = null, callId = null;

        Option username_option = Option.builder("u").longOpt("username").desc("The username for connecting to the IVR API").hasArg().build();
        Option password_option = Option.builder("p").longOpt("password").desc("The password for connecting to the IVR API").hasArg().build();
        Option callerid_option = Option.builder("id").longOpt("callerid").desc("The phone number to use for the caller id").hasArg().build();
        Option callid_option = Option.builder("n").longOpt("callid").desc("The IVR callid for this call").hasArg().build();

        Options options = new Options();
        options.addOption(username_option).addOption(password_option).addOption(callerid_option).addOption(callid_option);
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            if(line.hasOption("username")) user_param = line.getOptionValue("username");
            if(line.hasOption("password")) password_param = line.getOptionValue("password");
            if(line.hasOption("callerid")) callerId = line.getOptionValue("callerid");
            if(line.hasOption("callid")) callId = line.getOptionValue("callid");
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "ApiTester", options );
            return;
        }

        try {
            if(user_param == null || password_param == null) {
                System.err.println("Missing API credentials - Goodbye!");
                return;
            }
			cvSession = new ChoiceViewSession(ServerAddress, user_param, password_param);
            String command;
            if(callerId == null) {
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
                    params.close();
                }
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
                else if(param0.equalsIgnoreCase("CONTROLMSG")) {
                    Map<String, String> controlmsg = cvSession.getControlMessage();
                    if(controlmsg == null) {
                        System.out.println("No control message!");
                    } else {
                        System.out.println("Control message parameters:");
                        for(Entry<String, String> parameter : controlmsg.entrySet()) {
                            System.out.println(parameter.getKey() + ": " + parameter.getValue());
                        }
                        System.out.println();
                    }
                }
                else if(param0.equalsIgnoreCase("CLEARMSG")) {
                    if(!cvSession.clearControlMessage()) {
                        System.err.println("Cannot clear control message!");
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
                else if(param0.equalsIgnoreCase("ADD")) {
                    String param1 = params.hasNext() ? params.next() : null;
                    String param2 = params.hasNext() ? params.nextLine() : null;
                    if(param1 != null && param1.length() > 0) {
                        if(param2 != null && param2.length() > 0) {
                            if(cvSession.addProperty(param1.trim(), param2.trim())) {
                                System.out.println("Added property" + param1 + "='" + param2.trim() + ".");
                            } else {
                                System.err.println("Cannot add property " + param1 + " = [" + param2.trim() + "]");
                            }
                        } else {
                            System.err.println("Missing value for property " + param1 + "!");
                        }
                    } else {
                        System.err.println("Missing property key!");
                    }
                }
                params.close();
            } while(cvSession.updateSession() &&
                    !cvSession.getStatus().equals("disconnected"));
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
