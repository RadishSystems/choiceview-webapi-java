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
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

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
	static class Property {
		public String name;
		public String value;
		public Property() { this("", ""); }
		public Property(String name, String value) {
			this.name = name;
			this.value = value;
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
	static class Payload {
		final public Map<String, String> properties;
		final public List<Link> links;
		public Payload() {
			properties = new HashMap<String, String>();
			links = new ArrayList<Link>();
		}
	}
	
	private static boolean isSuccessful(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		return (statusCode > 199 && statusCode < 300);
	}
	
	ResponseHandler<Session> sessionHandler = new ResponseHandler<Session>() {
		public Session handleResponse(HttpResponse response) 
				throws ClientProtocolException, IOException {
			if(isSuccessful(response)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try {
					    return mapper.readValue(EntityUtils.toString(entity), Session.class);
					}
					finally {
						entity.getContent().close();
					}
				}
			} else {
				printErrorResponse(response);
			}
			return null;
		}
	};
	ResponseHandler<Map<String, String>> controlMessageHandler = new ResponseHandler<Map<String, String>>() {
		@SuppressWarnings("unchecked")
		public Map<String, String> handleResponse(HttpResponse response) 
				throws ClientProtocolException, IOException {
			if(isSuccessful(response)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try {
						String content = EntityUtils.toString(entity);
        				return (Map<String, String>) (isNullOrEmpty(content) ?
        						null : mapper.readValue(content, new TypeReference<Map<String, String>>() {}));
					}
					finally {
						entity.getContent().close();
					}
				}
			} else {
				printErrorResponse(response);
			}
			return null;
		}
	};
	ResponseHandler<Payload> payloadHandler = new ResponseHandler<Payload>() {
		public Payload handleResponse(HttpResponse response) 
				throws ClientProtocolException, IOException {
			if(isSuccessful(response)) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try {
					    return mapper.readValue(EntityUtils.toString(entity), Payload.class);
					}
					finally {
						entity.getContent().close();
					}
				}
			} else {
				printErrorResponse(response);
			}
			return null;
		}
	};
	ResponseHandler<Boolean> defaultHandler = new ResponseHandler<Boolean>() {
		public Boolean handleResponse(HttpResponse response)
		throws ClientProtocolException, IOException {
			if(!isSuccessful(response)) {
				printErrorResponse(response);
				return false;
			}
			return true;
		}
	};
	
	final static String StateNotificationRel = "/rels/statenotification";
	final static String MessageNotificationRel = "/rels/messagenotification";
	final static String SessionRel = "/rels/session";
	final static String PayloadRel = "/rels/properties";
	final static String ControlMessageRel = "/rels/controlmessage";
	
	private Session cvSession;
	
	private URI sessionsUri;
	private DefaultHttpClient client;
	private ObjectMapper mapper;
	private AuthCache authCache;
	
	private HttpContext getApiContext() {
        HttpContext context = null;
        if(authCache != null) {
        	context = new BasicHttpContext();
        	context.setAttribute(ClientContext.AUTH_CACHE, authCache);
        }
        return context;
	}
	
	public int getSessionId() { return cvSession.sessionId; }
	public String getCallerId() { return cvSession.callerId; }
	public String getCallId() { return cvSession.callId; }
	public String getStatus() { return cvSession.status; }
	public String getNetworkQuality() { return cvSession.networkQuality; }
	public String getNetworkType() { return cvSession.networkType; }
	public Map<String, String> getProperties() { return Collections.unmodifiableMap(cvSession.properties); }
	
	ChoiceViewSession(String serverAddress, int serverPort, boolean useHttps,
			String username, String password) {
		if(serverAddress == null) throw new IllegalArgumentException("No server address specified.");
		URIBuilder builder = new URIBuilder();
		builder.setScheme(useHttps ? "https" : "http").setHost(serverAddress).setPath("/ivr/api/sessions");
		if(serverPort > 0) {
			builder.setPort(serverPort);
		} else {
			serverPort = useHttps ? 443 : 80;
		}
		try {
			sessionsUri = builder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		client = new DefaultHttpClient(cm);
				
		if(username != null && password != null) {
			// Add basic authentication credentials
			client.getCredentialsProvider().setCredentials(
                    new AuthScope(serverAddress, serverPort),
                    new UsernamePasswordCredentials(username, password));
			
			authCache = new BasicAuthCache();
            authCache.put(new HttpHost(serverAddress, serverPort, useHttps ? "https" : "http"),
            		      new BasicScheme());
		}
		
		mapper = new ObjectMapper();
		cvSession = new Session();
	}

	// If username and password is specified, must use https
	public ChoiceViewSession(String serverAddress, int serverPort, String username, String password) {
		this(serverAddress, serverPort, true, username, password);
	}
	
	public ChoiceViewSession(String serverAddress, String username, String password) {
		this(serverAddress, 443, username, password);
	}
	
	public ChoiceViewSession(String serverAddress, int serverPort, boolean useHttps) {
		this(serverAddress, serverPort, useHttps, null, null);
	}
	
	public ChoiceViewSession(String serverAddress, boolean useHttps) {
		this(serverAddress, useHttps ? 443 : 80, useHttps, null, null);
	}
	
	public ChoiceViewSession(String serverAddress) {
		this(serverAddress, false);
	}

	public boolean startSession(String callerId, String callId) throws IOException {
		return startSession(callerId, callId, "", "", "");
	}

	public boolean startSession(String callerId, String callId,
			String stateChangeUriString, String newMessageUriString, String notificationType) throws IOException {
		URI stateChangeUri = stateChangeUriString != null && stateChangeUriString.length() > 0 ? URI.create(stateChangeUriString) : null;
		URI newMessageUri = newMessageUriString != null && newMessageUriString.length() > 0 ? URI.create(newMessageUriString) : null;
		return startSession(callerId, callId, stateChangeUri, newMessageUri, notificationType);
	}
	
	public boolean startSession(String callerId, String callId,
			URI stateChangeUri, URI newMessageUri, String notificationType) throws IOException {
		if(cvSession != null && cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("callerId", callerId);
		params.put("callId", callId);
		
		if(stateChangeUri != null) {
			if(stateChangeUri.isAbsolute() && !stateChangeUri.isOpaque()) {
				params.put("stateChangeUri", stateChangeUri.toString());
			} else {
				throw new IllegalArgumentException("Invalid stateChangeUri!");
			}
		}
		if(newMessageUri != null) {
			if(newMessageUri.isAbsolute() && !newMessageUri.isOpaque()) {
				params.put("newMessageUri", newMessageUri.toString());
			} else {
				throw new IllegalArgumentException("Invalid newMessageUri!");
			}
		}
		if(notificationType != null && notificationType.length() > 0) {
			if("basic".equalsIgnoreCase(notificationType) || "CCXML".equalsIgnoreCase(notificationType)) {
				params.put("notificationType", notificationType);
			} else {
				throw new IllegalArgumentException("Invalid notificationType!");
			}
		}
		
		HttpPost request = new HttpPost(sessionsUri);
		request.setEntity(new StringEntity(mapper.writeValueAsString(params),
				ContentType.create("application/json", "utf-8")));
		request.addHeader("ACCEPT", "application/json");
		try {
			Session newSession = client.execute(request, sessionHandler, getApiContext());
			if(newSession != null) {
				cvSession = newSession;
				return true;
			}
		} catch(RuntimeException e)	{
			request.abort();
			throw e;
		}
		return false;
	}
	
	public boolean endSession() throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		URI selfUri = getSessionUri();
		if(selfUri != null) {
			HttpDelete request = new HttpDelete(selfUri);
			try {
				if(client.execute(request, defaultHandler, getApiContext())) {
					cvSession.status = "disconnected";
					return true;
				}
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}		
		return false;
	}
	
	public boolean updateSession() throws IOException {
		if(cvSession == null) {
			return false;
		}
		
		URI selfUri = getSessionUri();
		if(selfUri != null) {
			HttpGet request = new HttpGet(selfUri);
			request.addHeader("ACCEPT", "application/json");
			try {
				Session newSession = client.execute(request, sessionHandler, getApiContext());
				if(newSession != null) {
					cvSession = newSession;
				} else {
					cvSession.status = "disconnected";
				}
				return true;
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return false;
	}
	
	public boolean transferSession(String accountId) throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		URI selfUri = getSessionUri();
		if(selfUri != null && accountId != null && accountId.length() > 0) {
			URI xferUri;
			try {
				xferUri = new URI(selfUri.toString() + "/transfer/" + accountId);
			} catch (URISyntaxException e1) {
				return false;
			}
			HttpPost request = new HttpPost(xferUri);
			try {
				if(client.execute(request, defaultHandler, getApiContext())) {
					// connection is gone
					cvSession.status = "disconnected";
					return true;
				}
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return false;
	}
	
	public boolean sendUrl(String url) throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected") ||
		   url == null || url.length() == 0) {
			return false;
		}
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("url", url);
		
		URI selfUri = getSessionUri();
		if(selfUri != null) {
			HttpPost request = new HttpPost(selfUri);
			request.setEntity(new StringEntity(mapper.writeValueAsString(params),
					ContentType.create("application/json", "utf-8")));
			try {
				return client.execute(request, defaultHandler, getApiContext());
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return false;
	}
	
	public boolean sendText(String msg) throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected") ||
		   msg == null || msg.length() == 0) {
			return false;
		}
		
		URI selfUri = getSessionUri();
		if(selfUri != null) {
			HttpPost request = new HttpPost(selfUri);
			request.setEntity(new StringEntity(msg,
					ContentType.create("text/plain", "utf-8")));
			try {
				return client.execute(request, defaultHandler, getApiContext());
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return false;
	}
	
	public Map<String, String> getControlMessage() throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return null;
		}
		
		URI apiUri = getControlMessageUri();
		if(apiUri != null) {
			HttpGet request = new HttpGet(apiUri);
			try {
				Map<String, String> msg = client.execute(request, controlMessageHandler, getApiContext());
				if(msg != null && !msg.isEmpty()) { clearControlMessage(); }
				return msg;
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return null;
	}
	
	public boolean clearControlMessage() throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		URI apiUri = getControlMessageUri();
		if(apiUri != null) {
			HttpDelete request = new HttpDelete(apiUri);
			try {
				return client.execute(request, defaultHandler, getApiContext());
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return false;
	}
	
	public Map<String, String> updateProperties() throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return null;
		}
		
		URI apiUri = getPayloadUri();
		if(apiUri != null) {
			HttpGet request = new HttpGet(apiUri);
			try {
				Payload payload = client.execute(request, payloadHandler, getApiContext());
				if(payload != null && !payload.properties.equals(cvSession.properties)) {
					cvSession.properties.putAll(payload.properties);
				}
				return payload.properties;
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return null;
	}
	
	public boolean addProperties(Map<String, String> properties) throws IOException {
		if(properties == null || properties.size() == 0) {
			return false;
		}
		
		for(Map.Entry<String, String> pair : properties.entrySet()) {
			if(!addProperty(pair.getKey(), pair.getValue())) {
				return false;
			}
		}
		return true;
	}
	
	public boolean addProperty(String name, String value) throws IOException {
		return addProperty(new Property(name, value));
	}
	
	public boolean addProperties(Property[] properties) throws IOException {
		for(Property p : properties) {
			if(!addProperty(p)) {
				return false;
			}
		}
		return true;
	}

	public boolean addProperty(Property p) throws IOException {
		if(cvSession == null || !cvSession.status.equalsIgnoreCase("connected")) {
			return false;
		}
		
		if(isNullOrEmpty(p.name) || cvSession.properties.containsKey(p.name)) {
			return false;
		}
		if(isNullOrEmpty(p.value)) {
			return false;
		}
		
		URI apiUri = getPayloadUri();
		if(apiUri != null) {
			HttpPost request = new HttpPost(apiUri);
			request.setEntity(new StringEntity(mapper.writeValueAsString(p),
					ContentType.create("application/json", "utf-8")));
			try {
				return client.execute(request, defaultHandler, getApiContext());
			} catch(RuntimeException e)	{
				request.abort();
				throw e;
			}
		}
		return false;
	}
	
	private URI getUri(String rel) {
		URI selfUri = null;
		for(Link l : cvSession.links) {
			if(l.rel.equalsIgnoreCase(rel)) {
				try {
					selfUri = new URI(l.href);
				} catch (URISyntaxException e) {
					System.err.println("Cannot parse API uri " + l.href);
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
	
	private URI getPayloadUri() {
		return getUri(PayloadRel);
	}
	
	private static boolean isNullOrEmpty(String value) {
		return (value == null || value.length() == 0);
	}
	
	private static void printErrorResponse(HttpResponse response) {
		System.err.println(String.format("HTTP request failed: %s (%d)",
				response.getStatusLine().getReasonPhrase(),
				response.getStatusLine().getStatusCode()));
	}
}
