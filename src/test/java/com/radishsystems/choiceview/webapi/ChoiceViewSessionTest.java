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
import org.junit.BeforeClass;
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
	static class Payload {
		final public Map<String, String> properties;
		final public List<Link> links;
		public Payload() {
			properties = new HashMap<String, String>();
			properties.put("TestKey1", "UpdatedTestValue");
			properties.put("TestKey2", "UpdatedTestValue");
			links = new ArrayList<Link>();
		}
	}
	
	final static int expectedSessionId = 1001;
	final static String expectedCallerId = "7202950840";
	final static String expectedCallId = "12345";
	final static String expectedStateChangeUrl = "http://test.ivr.com/1001/state_change";
	final static String expectedNewMessageUrl = "http://test.ivr.com/1001/new_message";
	final static Map<String, String> expectedControlMessage = new HashMap<String, String>();

	private ChoiceViewSession testSession;
	private LocalTestServer testServer;
	private ObjectMapper mapper;
	
	private boolean activeSession = false;
	private boolean controlMessageAvailable = false;
	
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
					session.links.add(new Link(ChoiceViewSession.PayloadRel,
					          selfUri + "/properties"));
					response.setEntity(new StringEntity(mapper.writeValueAsString(session),
							ContentType.create("application/json", "utf-8")));
					activeSession = true;
					controlMessageAvailable = true;
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
				if(activeSession) {
					activeSession = false;
					controlMessageAvailable = false;
					response.setStatusCode(200);
				} else {
					response.setStatusCode(404);
				}
			}
			if(method.equals("GET")) {
				if(!activeSession) {
					response.setStatusCode(404);
					return;
				}
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
				session.links.add(new Link(ChoiceViewSession.PayloadRel,
				          sessionUri + "/properties"));
				if(request.containsHeader("ACCEPT")) {
					String acceptHeader = request.getFirstHeader("ACCEPT").getValue();
					if(acceptHeader != null && !acceptHeader.toUpperCase().contains("JSON")) {
						throw new ProtocolException(acceptHeader + " type not supported!");
					}
				}
				response.setEntity(new StringEntity(mapper.writeValueAsString(session),
						ContentType.create("application/json", "utf-8")));
				return;
			}
			if(method.equals("POST")) {
				if(!activeSession) {
					response.setStatusCode(404);
					return;
				}
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
				return;
			}
		}
	};
	
	private HttpRequestHandler propertiesHandler = new HttpRequestHandler() {
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {
			String method = request.getRequestLine().getMethod().toUpperCase();
			if(method.equals("PUT")) {
				throw new MethodNotSupportedException("PUT request not supported for properties");
			}
			if(method.equals("DELETE")) {
				throw new MethodNotSupportedException("DELETE request not supported for properties");
			}
			if(method.equals("GET")) {
				if(!activeSession) {
					response.setStatusCode(404);
					return;
				}
				String payloadUri = "http://" + testServer.getServiceAddress().getHostName() + ":" +
						testServer.getServiceAddress().getPort() + request.getRequestLine().getUri();
				String sessionUri = payloadUri.replace("/properties", "");
				Payload payload = new Payload();
				payload.links.add(new Link("self", payloadUri));
				payload.links.add(new Link(ChoiceViewSession.SessionRel,
						          sessionUri));
				if(request.containsHeader("ACCEPT")) {
					String acceptHeader = request.getFirstHeader("ACCEPT").getValue();
					if(acceptHeader != null && !acceptHeader.toUpperCase().contains("JSON")) {
						throw new ProtocolException(acceptHeader + " type not supported!");
					}
				}
				response.setEntity(new StringEntity(mapper.writeValueAsString(payload),
						ContentType.create("application/json", "utf-8")));
            	response.setStatusCode(200);
				return;
			}
			if(method.equals("POST")) {
				if(!activeSession) {
					response.setStatusCode(404);
					return;
				}
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
	        				if(params != null && params.size() == 2 && params.containsKey("name")) {
			                	response.setStatusCode(200);
			                	return;
	        				}
	                	}
						throw new ProtocolException("No client property in POST request!");	                	
	                } else {
		                throw new ProtocolException("No content-type header in POST request!");
	                }
				} else {
					throw new ProtocolException("No content in POST request!");
				}
			}
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
				if(activeSession) {
					if(controlMessageAvailable) {
						response.setStatusCode(200);
						controlMessageAvailable = false;
					} else {
						response.setStatusCode(304);
					}
				} else {
					response.setStatusCode(404);
				}
			}
			if(method.equals("GET")) {
				if(!activeSession) {
					response.setStatusCode(404);
				} else {
					response.setStatusCode(200);
					if(controlMessageAvailable) {
						response.setEntity(new StringEntity(mapper.writeValueAsString(expectedControlMessage),
								ContentType.create("application/json", "utf-8")));
					}
				}
			}
			if(method.equals("POST")) {
				throw new MethodNotSupportedException("POST request not supported for control message");
			}
		}
	};
	
	private HttpRequestHandler transferHandler = new HttpRequestHandler() {
		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {
			String method = request.getRequestLine().getMethod().toUpperCase();
			if(method.equals("PUT")) {
				throw new MethodNotSupportedException("PUT request not supported for transfers");
			}
			if(method.equals("DELETE")) {
				throw new MethodNotSupportedException("DELETE request not supported for transfers");
			}
			if(method.equals("GET")) {
				throw new MethodNotSupportedException("GET request not supported for transfers");
			}
			if(method.equals("POST")) {
				if(activeSession) {
					activeSession = false;
					response.setStatusCode(200);
				} else {
					response.setStatusCode(404);
				}
			}
		}
	};

	@BeforeClass
	public static void onlyOnce() {
		expectedControlMessage.put("MenuNumber", "1");
		expectedControlMessage.put("MenuName", "Radish_Main_Menu");
		expectedControlMessage.put("ButtonNumber", "0");
		expectedControlMessage.put("ButtonName", "ChoiceView+Demo");
	}
	
	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
		
		testServer = new LocalTestServer(null, null);
		String baseUri = "/ivr/api/session/";
		String sessionUri = baseUri + expectedSessionId;
		testServer.register("/ivr/api/sessions", sessionsHandler);
		testServer.register(sessionUri, sessionHandler);
		testServer.register(sessionUri + "/controlmessage", controlMessageHandler);
		testServer.register(sessionUri + "/properties", propertiesHandler);
		testServer.register(sessionUri + "/transfer/*", transferHandler);
		testServer.start();
		
		testSession = new ChoiceViewSession(testServer.getServiceAddress().getHostName(),
				testServer.getServiceAddress().getPort(), false);
	}

	@Test
	public void testStartSession() throws IOException {
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		assertEquals(expectedSessionId, testSession.getSessionId());
		assertEquals(expectedCallerId, testSession.getCallerId());
		assertEquals(expectedCallId, testSession.getCallId());
	}

	@Test
	public void testStartSessionWithGoodUrls() throws IOException {
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId, expectedStateChangeUrl, expectedNewMessageUrl, "basic"));
		assertEquals(expectedSessionId, testSession.getSessionId());
		assertEquals(expectedCallerId, testSession.getCallerId());
		assertEquals(expectedCallId, testSession.getCallId());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testStartSessionWithGoodUrlsAndBadNotificationType() throws IOException {
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId, expectedStateChangeUrl, expectedNewMessageUrl, "badType"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testStartSessionWithBadUrls() throws IOException {
		assertFalse(testSession.startSession(expectedCallerId, expectedCallId, "Bad url 1", "Bad url 2", "ccxml"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testStartSessionWithOpaqueUrls() throws IOException {
		assertFalse(testSession.startSession(expectedCallerId, expectedCallId, "mailto:dfjacobs@yahoo.com", "mailto:dfjacobs@sshores.com", "basic"));
	}

	@Test
	public void testEndSession() throws IOException {
		// EndSession fails if no session
		assertFalse(testSession.endSession());
		
		// EndSession succeeds if the session is active
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		assertTrue(testSession.endSession());
		// EndSession fails if the session is not active
		assertFalse(testSession.endSession());
	}

	@Test
	public void testUpdateSessionWhenIVREndsSession() throws IOException {
		// UpdateSession fails if no session
		assertFalse(testSession.updateSession());
		
		// UpdateSession succeeds if session is started
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		assertTrue(testSession.updateSession());
		assertEquals(expectedSessionId, testSession.getSessionId());
		assertEquals(expectedCallerId, testSession.getCallerId());
		assertEquals(expectedCallId, testSession.getCallId());
		// UpdateSession succeeds if the session has ended
		assertTrue(testSession.endSession());
		assertTrue(testSession.updateSession());
		assertTrue("disconnected".equals(testSession.getStatus()));
	}

	@Test
	public void testUpdateSessionWhenSwitchEndsSession() throws IOException {
		// UpdateSession succeeds if the session is ended by the server
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		assertTrue("connected".equals(testSession.getStatus()));
		activeSession = false;
		assertTrue(testSession.updateSession());
		assertTrue("disconnected".equals(testSession.getStatus()));
	}

	@Test
	public void testTransferSessionWithBadAccountId() throws IOException {
		String badAccountId = "";
		
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		
		// TransferSession fails if accountId is not specified
		assertFalse(testSession.transferSession(badAccountId));
		assertFalse(testSession.transferSession(null));
	}
	
	@Test
	public void testTransferSessionWithGoodAccountId() throws IOException {
		String expectedAccountId = "test1";
		
		// TransferSession fails if no session
		assertFalse(testSession.transferSession(expectedAccountId));
		
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		
		// TransferSession succeeds if the session is active
		assertTrue(testSession.transferSession(expectedAccountId));
		// After successful TransferSession, connection state is disconnected
		assertTrue(testSession.getStatus().equals("disconnected"));
		// TransferSession fails if session has ended
		assertFalse(testSession.transferSession(expectedAccountId));
	}

	@Test
	public void testSendUrl() throws IOException {
		// SendUrl fails if no session
		assertFalse(testSession.sendUrl("http://www.radishsystems.com/"));
		
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		// SendUrl fails if no url
		assertFalse(testSession.sendUrl(""));
		assertFalse(testSession.sendUrl(null));
		// SendUrl succeeds if url is specified
		assertTrue(testSession.sendUrl("http://www.radishsystems.com/"));
		// SendUrl fails if session has ended
		assertTrue(testSession.endSession());
		assertFalse(testSession.sendUrl("http://www.radishsystems.com/"));
	}

	@Test
	public void testSendText() throws IOException {
		// SendText fails if no session
		assertFalse(testSession.sendText("How may I help you?"));
		
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		// SendText fails if no text
		assertFalse(testSession.sendText(""));
		assertFalse(testSession.sendText(null));
		// SendText succeeds if text is specified
		assertTrue(testSession.sendText("How may I help you?"));
		// SendText fails if session has ended
		assertTrue(testSession.endSession());
		assertFalse(testSession.sendText("How may I help you?"));
	}

	@Test
	public void testGetControlMessage() throws IOException {
		// GetControlMessage returns null if no session
		assertNull(testSession.getControlMessage());
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		// GetControlMessage returns message if session is active
		Map<String, String> actualControlMessage = testSession.getControlMessage();
		assertEquals(expectedControlMessage, actualControlMessage);
		// Once GetControlMessage reads the message, it is cleared
		assertNull(testSession.getControlMessage());
		// GetControlMessage returns null if session has ended
		controlMessageAvailable = true;
		assertTrue(testSession.endSession());
		assertNull(testSession.getControlMessage());
	}

	@Test
	public void testClearControlMessage() throws IOException {
		// ClearControlMessage fails if no session
		assertFalse(testSession.clearControlMessage());
		
		// ClearControlMessage succeeds if the session is active
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		assertTrue(testSession.clearControlMessage());
		// ClearControlMessage fails if there is no message
		assertFalse(testSession.clearControlMessage());
		// ClearControlMessage fails if session has ended
		controlMessageAvailable = true;
		assertTrue(testSession.endSession());
		assertFalse(testSession.clearControlMessage());
	}

	@Test
	public void testUpdateProperties() throws IOException {
		final Map<String, String> expectedProperties = new HashMap<String, String>();
		expectedProperties.put("TestKey1", "UpdatedTestValue");
		expectedProperties.put("TestKey2", "UpdatedTestValue");

		// UpdateProperties fails if no session
		assertNull(testSession.updateProperties());
		// UpdateProperties succeeds if the session is active
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		Map<String, String> actualProperties = testSession.updateProperties();
		assertEquals(expectedProperties, actualProperties);
		// UpdateProperties returns null if session has ended
		assertTrue(testSession.endSession());
		assertNull(testSession.updateProperties());
	}

	@Test
	public void testAddProperties() throws IOException {
		final Map<String, String> duplicateProperties = new HashMap<String, String>();
		duplicateProperties.put("TestKey1", "UpdatedTestValue");
		duplicateProperties.put("TestKey2", "UpdatedTestValue");
		
		final Map<String, String> goodProperties = new HashMap<String, String>();
		goodProperties.put("TestKey3", "NewTestValue");
		goodProperties.put("TestKey4", "NewTestValue");
		
		final Map<String, String> goodProperties2 = new HashMap<String, String>();
		goodProperties2.put("TestKey5", "NewTestValue");
		goodProperties2.put("TestKey6", "NewTestValue");

		// AddProperties fails if no session
		assertFalse(testSession.addProperties(duplicateProperties));
		// AddProperties succeeds if the session is active
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		assertFalse(testSession.addProperties(duplicateProperties));
		assertTrue(testSession.addProperties(goodProperties));
		// AddProperties fails if session is ended
		assertTrue(testSession.endSession());
		assertFalse(testSession.addProperties(goodProperties2));
	}

	@Test
	public void testAddProperty() throws IOException {
		String badName = "TestKey1";
		String goodName = "TestKey3";
		String goodName2 = "TestKey4";
		String testValue = "TestValue";
		
		// AddProperty fails if no session
		assertFalse(testSession.addProperty(goodName, testValue));
		// AddProperty succeeds if the session is active
		assertTrue(testSession.startSession(expectedCallerId, expectedCallId));
		assertFalse(testSession.addProperty(badName, testValue));
		assertTrue(testSession.addProperty(goodName, testValue));
		// AddProperty fails if the session is ended
		assertTrue(testSession.endSession());
		assertFalse(testSession.addProperty(goodName2, testValue));
	}
	
	@After
	public void tearDown() throws Exception {
		testServer.stop();
	}
}
