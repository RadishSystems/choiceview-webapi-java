package com.radishsystems.choiceview.webapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class ChoiceViewSession {
	
	static class Link {
		public String rel;
		public String href;
		public Link() { this("", ""); }
		public Link(String rel, String href) {
			this.rel = rel;
			this.href = href;
		}
	}
	static class Session {
		public int sessionId;
		public String callerId;
		public String callId;
		public String status;
		public String networkQuality;
		public String networkType;
		final public Map<String, String> properties;
		final public List<Link> links;
		public Session() {
			sessionId = 0;
			callerId = "";
			callId = "";
			status = "disconnected";
			networkQuality = "";
			networkType = "";
			properties = new HashMap<String, String>();
			links = new ArrayList<Link>();
		}
	}
	ResponseHandler<Session> sessionHandler = new ResponseHandler<Session>() {
		public Session handleResponse(HttpResponse response) 
				throws ClientProtocolException, IOException {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode > 199 && statusCode < 300) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
				    return mapper.readValue(EntityUtils.toString(entity), Session.class);
				}
			}
			return null;
		}
	};
	ResponseHandler<String> controlMessageHandler = new ResponseHandler<String>() {
		public String handleResponse(HttpResponse response) 
				throws ClientProtocolException, IOException {
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode > 199 && statusCode < 300) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
				    return EntityUtils.toString(entity);
				}
			}
			return null;
		}
	};
	
	final static String StateNotificationRel = "/rels/statenotification";
	final static String MessageNotificationRel = "/rels/messagenotification";
	final static String SessionRel = "/rels/session";
	final static String PayloadRel = "/rels/properties";
	final static String ControlMessageRel = "rels/controlmessage";
	
	private Session cvSession;
	
	private URI sessionsUri;
	private HttpClient client;
	private ObjectMapper mapper;
	
	public int GetSessionId() { return cvSession.sessionId; }
	public String GetCallerId() { return cvSession.callerId; }
	public String GetCallId() { return cvSession.callId; }
	public String GetStatus() { return cvSession.status; }
	public String GetNetworkQuality() { return cvSession.networkQuality; }
	public String GetNetworkType() { return cvSession.networkType; }
	public Map<String, String> GetProperties() { return Collections.unmodifiableMap(cvSession.properties); }
	
	public ChoiceViewSession(String serverAddress, int serverPort, boolean useHttps) {
		if(serverAddress == null) throw new IllegalArgumentException("No server address specified.");
		URIBuilder builder = new URIBuilder();
		builder.setScheme(useHttps ? "https" : "http").setHost(serverAddress).setPath("/ivr/api/sessions");
		if(serverPort > 0) {
			builder.setPort(serverPort);
		}
		try {
			sessionsUri = builder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		cvSession = new Session();
		client = new DefaultHttpClient();
		mapper = new ObjectMapper();
	}
	
	public ChoiceViewSession(String serverAddress, boolean useHttps) {
		this(serverAddress, useHttps ? 443 : 80, useHttps);
	}
	
	public ChoiceViewSession(String serverAddress) {
		this(serverAddress, false);
	}
	
	public boolean StartSession(String callerId, String callId) {
		if(cvSession != null && cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("callerId", callerId);
			params.put("callId", callId);
			
			HttpPost request = new HttpPost(sessionsUri);
			request.setEntity(new StringEntity(mapper.writeValueAsString(params),
					ContentType.create("application/json", "utf-8")));
			request.addHeader("ACCEPT", "application/json");
			Session newSession = client.execute(request, sessionHandler);
			if(newSession != null) {
				cvSession = newSession;
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	public boolean EndSession() {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		URI selfUri = getSessionUri();
		if(selfUri != null) {
			try {
				HttpDelete request = new HttpDelete(selfUri);
				HttpResponse response = client.execute(request);
				if(response.getStatusLine().getStatusCode() == 200) {
					cvSession.status = "disconnected";
					return true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return false;
	}
	
	public boolean UpdateSession() {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		try {
			URI selfUri = getSessionUri();
			if(selfUri != null) {
				HttpGet request = new HttpGet(selfUri);
				request.addHeader("ACCEPT", "application/json");
				Session newSession = client.execute(request, sessionHandler);
				if(newSession != null) {
					cvSession = newSession;
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	public boolean SendUrl(String url) {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected") ||
		   url == null || url.length() == 0) {
			return false;
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("url", url);
		
		try {
			URI selfUri = getSessionUri();
			if(selfUri != null) {
				HttpPost request = new HttpPost(selfUri);
				request.setEntity(new StringEntity(mapper.writeValueAsString(params),
						ContentType.create("application/json", "utf-8")));
				HttpResponse response = client.execute(request);
				return response.getStatusLine().getStatusCode() == 200;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	public boolean SendText(String msg) {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected") ||
		   msg == null || msg.length() == 0) {
			return false;
		}
		
		try {
			URI selfUri = getSessionUri();
			if(selfUri != null) {
				HttpPost request = new HttpPost(selfUri);
				request.setEntity(new StringEntity(msg,
						ContentType.create("text/plain", "utf-8")));
				HttpResponse response = client.execute(request);
				return response.getStatusLine().getStatusCode() == 200;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	public String GetControlMessage() {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return null;
		}
		
		try {
			URI apiUri = getControlMessageUri();
			if(apiUri != null) {
				HttpGet request = new HttpGet(apiUri);
				return client.execute(request, controlMessageHandler);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
	
	private URI getUri(String rel) {
		URI selfUri = null;
		for(Link l : cvSession.links) {
			if(l.rel.equalsIgnoreCase(rel)) {
				try {
					selfUri = new URI(l.href);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		return selfUri;
	}
	
	private URI getSessionUri() {
		return getUri("self");
	}
	
	private URI getControlMessageUri() {
		return getUri(ControlMessageRel);
	}
}
