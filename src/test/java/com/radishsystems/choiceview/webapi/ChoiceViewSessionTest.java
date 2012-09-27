package com.radishsystems.choiceview.webapi;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.ProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChoiceViewSessionTest {
	
	static class Link {
		public String rel;
		public String href;
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
		public Map<String, String> properties;
		public List<Link> links;
		public Session() {
			sessionId = expectedSessionId;
			status = "connected";
			networkQuality = "excellent";
			networkType = "WiFi";
			properties = new HashMap<String, String>();
			properties.put("TestKey1", "TestValue");
			properties.put("TestKey2", "TestValue");
			links = new ArrayList<Link>();
		}
	}
	
	final static int expectedSessionId = 1001;
	final static String expectedCallerId = "7202950840";
	final static String expectedCallId = "12345";
	final static String expectedControlMessage = "1, Radish_Main_Menu, 0, ChoiceView+Demo";

	private ChoiceViewSession testSession;
	private LocalTestServer testServer;
	private ObjectMapper mapper;
	
	private HttpRequestHandler sessionsHandler = new HttpRequestHandler() {
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {
			String method = request.getRequestLine().getMethod().toUpperCase();
			if(method.equals("PUT") || method.equals("GET")) {
				throw new MethodNotSupportedException(method + " request not supported for sessions");
			}
			if(method.equals("DELETE")) {
				response.setStatusCode(403);
			}
			if(method.equals("POST")) {
				String content = null;
				Map<String, String> params = null;
				String requestUri = request.getRequestLine().getUri();
				String selfUri = "http://" + testServer.getServiceAddress().getHostName() +
						":" + testServer.getServiceAddress().getPort() + "/" +
						requestUri.substring(0, requestUri.length()-1) + "/" + expectedSessionId;
				if(request instanceof HttpEntityEnclosingRequest) {
	                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
	                content = EntityUtils.toString(entity);
				}
				if(content != null) {
	                if(request.containsHeader("Content-Type")) {
	                	String value = request.getFirstHeader("Content-Type").getValue();
	                	if(value == null) {
	    					throw new ProtocolException("No content type specified in POST request!");
	                	}
	                	if(value.toUpperCase().contains("JSON")) {
							params = mapper.readValue(content,
									new TypeReference<Map<String, String>>() {});
	                	} else {
	                		throw new ProtocolException("Content is not json!");
	                	}
	                } else {
	                	throw new ProtocolException("No Content-Type header!");
	                }
				}
				if(params == null) {
					throw new ProtocolException("No content in POST request!");
				}
				
				if(params.containsKey("callerId") && params.get("callerId") != null) {
					response.setStatusCode(201);
					Session session = new Session();
					session.callerId = params.get("callerId");
					session.callId = params.containsKey("callId") ? params.get("callId") : "";
					session.links.add(new Link("self",selfUri));
					session.links.add(new Link(ChoiceViewSession.ControlMessageRel,
							          selfUri + "/controlmessage"));
					response.setEntity(new StringEntity(mapper.writeValueAsString(session),
							ContentType.create("application/json", "utf-8")));

				} else {
					throw new ProtocolException("No caller id in POST request content!");
				}
			}
		}
	};
	
	private HttpRequestHandler sessionHandler = new HttpRequestHandler() {
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {
			String method = request.getRequestLine().getMethod().toUpperCase();
			if(method.equals("PUT")) {
				throw new MethodNotSupportedException("PUT request not supported for sessions");
			}
			if(method.equals("DELETE")) {
				response.setStatusCode(200);
			}
			if(method.equals("GET")) {
				String sessionUri = "http://" + testServer.getServiceAddress().getHostName() + ":" +
						testServer.getServiceAddress().getPort() + "/" +
						request.getRequestLine().getUri();
				response.setStatusCode(200);
				Session session = new Session();
				session.callerId = expectedCallerId;
				session.callId = expectedCallId;
				session.links.add(new Link("self", sessionUri));
				session.links.add(new Link(ChoiceViewSession.ControlMessageRel,
						          sessionUri + "/controlmessage"));
				if(request.containsHeader("ACCEPT")) {
					String acceptHeader = request.getFirstHeader("ACCEPT").getValue();
					if(acceptHeader != null && !acceptHeader.toUpperCase().contains("JSON")) {
						throw new ProtocolException(acceptHeader + " type not supported!");
					}
				}
				response.setEntity(new StringEntity(mapper.writeValueAsString(session),
						ContentType.create("application/json", "utf-8")));
			}
			if(method.equals("POST")) {
				String content = null;
				if(request instanceof HttpEntityEnclosingRequest) {
	                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
	                content = EntityUtils.toString(entity);
					if(content == null) {
						throw new ProtocolException("Cannot retrieve content from POST request!");
					}
	                if(request.containsHeader("Content-Type")) {
	                	String value = request.getFirstHeader("Content-Type").getValue().toLowerCase();
	                	if(value == null) {
	    					throw new ProtocolException("No content type specified in POST request!");
	                	}
	                	if(value.contains("application/json")) {
	        				Map<String, String> params = mapper.readValue(content,
	    							new TypeReference<Map<String, String>>() {});
	        				if(params.keySet().size() != 1 || !params.containsKey("url")) {
	    						throw new ProtocolException("No url in POST request!");
	        				}
	                	}
	                	response.setStatusCode(200);
	                } else {
		                throw new ProtocolException("No content-type header in POST request!");
	                }
				} else {
					throw new ProtocolException("No content in POST request!");
				}
			}
		}
	};
	
	private HttpRequestHandler propertiesHandler = new HttpRequestHandler() {
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {
			// TODO Auto-generated method stub
			
		}
	};
	
	private HttpRequestHandler controlMessageHandler = new HttpRequestHandler() {
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {
			String method = request.getRequestLine().getMethod().toUpperCase();
			if(method.equals("PUT")) {
				throw new MethodNotSupportedException("PUT request not supported for control message");
			}
			if(method.equals("DELETE")) {
				response.setStatusCode(200);
			}
			if(method.equals("GET")) {
				response.setStatusCode(200);
				response.setEntity(new StringEntity(expectedControlMessage,
						ContentType.create("text/plain", "utf-8")));
			}
			if(method.equals("POST")) {
				throw new MethodNotSupportedException("POST request not supported for control message");
			}
		}
	};
	
	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
		
		testServer = new LocalTestServer(null, null);
		testServer.register("/ivr/api/sessions", sessionsHandler);
		testServer.register("/ivr/api/session/" + expectedSessionId, sessionHandler);
		testServer.register("/ivr/api/session/" + expectedSessionId + "/controlmessage",
				            controlMessageHandler);
		testServer.start();
		
		testSession = new ChoiceViewSession(testServer.getServiceAddress().getHostName(),
				testServer.getServiceAddress().getPort(), false);
	}

	@Test
	public void testStartSession() {
		assertTrue(testSession.StartSession(expectedCallerId, expectedCallId));
		assertEquals(expectedSessionId, testSession.GetSessionId());
		assertEquals(expectedCallerId, testSession.GetCallerId());
		assertEquals(expectedCallId, testSession.GetCallId());
	}

	@Test
	public void testEndSession() {
		// EndSession fails if no session
		assertFalse(testSession.EndSession());
		
		// EndSession succeeds if the session is active
		assertTrue(testSession.StartSession(expectedCallerId, expectedCallId));
		assertTrue(testSession.EndSession());
	}

	@Test
	public void testUpdateSession() {
		// UpdateSession fails if no session
		assertFalse(testSession.UpdateSession());
		
		// UpdateSession succeeds if session is started
		assertTrue(testSession.StartSession(expectedCallerId, expectedCallId));
		assertTrue(testSession.UpdateSession());
		assertEquals(expectedSessionId, testSession.GetSessionId());
		assertEquals(expectedCallerId, testSession.GetCallerId());
		assertEquals(expectedCallId, testSession.GetCallId());
	}

	@Test
	public void testSendUrl() {
		// SendUrl fails if no session
		assertFalse(testSession.SendUrl("http://www.radishsystems.com/"));
		
		assertTrue(testSession.StartSession(expectedCallerId, expectedCallId));
		// SendUrl fails if no url
		assertFalse(testSession.SendUrl(""));
		assertFalse(testSession.SendUrl(null));
		
		assertTrue(testSession.SendUrl("http://www.radishsystems.com/"));
	}

	@Test
	public void testSendText() {
		// SendUrl fails if no session
		assertFalse(testSession.SendText("How may I help you?"));
		
		assertTrue(testSession.StartSession(expectedCallerId, expectedCallId));
		// SendUrl fails if no url
		assertFalse(testSession.SendText(""));
		assertFalse(testSession.SendText(null));
		
		assertTrue(testSession.SendText("How may I help you?"));
	}

	@Test
	public void testGetControlMessage() {
		assertNull(testSession.GetControlMessage());
		assertTrue(testSession.StartSession(expectedCallerId, expectedCallId));
		String actualControlMessage = testSession.GetControlMessage();
		assertEquals(expectedControlMessage, actualControlMessage);
	}

	@After
	public void tearDown() throws Exception {
		testServer.stop();
	}
}
