package com.chat.api.controller;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.chat.api.model.ChatMessage;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatControllerTest {

	@Value("${local.server.port}")
	private int port;
	private String URL;
	
	private static final int PUBLIC_TOPIC = 0;
	private static final String SUBSCRIBE_CHAT_MESSAGE = "/topic/chat/sended/";
	private static final String REGISTER_CHAT_ENDPOINT = "/app/chat/register/";
	
	private static final String SEND_MESSAGE_ENDPOINT = "/app/chat/send/";
	
	private static final String SUBSCRIBE_TOPICS = "/app/chat/topics/subscribe";
	
	private CompletableFuture<ChatMessage> completableFutureChatMessage;
	private CompletableFuture<List<?>> completableFutureString;
	
	@Before
    public void before() {
		completableFutureChatMessage = new CompletableFuture<>();
		completableFutureString = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/chat";
    }
	
	@Test
    public void testRegisterUserEndpoint() {
		try {
	        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
	        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	
	        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
	        
	        assertTrue(stompSession.isConnected());
	
	        stompSession.subscribe(SUBSCRIBE_CHAT_MESSAGE + PUBLIC_TOPIC, new CreateChatMessageStompFrameHandler());
	        
	        stompSession.send(REGISTER_CHAT_ENDPOINT + PUBLIC_TOPIC, new ChatMessage("hi", "lucas", ChatMessage.MessageType.JOIN, 0, 0));
	
	        ChatMessage chatMessage = completableFutureChatMessage.get(5, SECONDS);
	        
	        assertNotNull(chatMessage);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Not able to register user.");
		}
    }
	
	@Test
    public void testSendMessageEndpoint() {
		try {
	        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
	        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	
	        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
	        
	        assertTrue(stompSession.isConnected());
	
	        stompSession.subscribe(SUBSCRIBE_CHAT_MESSAGE + PUBLIC_TOPIC, new CreateChatMessageStompFrameHandler());
	        
	        stompSession.send(SEND_MESSAGE_ENDPOINT + PUBLIC_TOPIC, new ChatMessage("Hello, how are you?", "lucas", ChatMessage.MessageType.CHAT, 0, 0));
	
	        ChatMessage chatMessage = completableFutureChatMessage.get(5, SECONDS);
	        
	        assertNotNull(chatMessage);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Not able to send a message.");
		}
    }
	
	@Test
    public void testTopicIsSented() {
		try {
	        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
	        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	
	        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {}).get(1, SECONDS);
	        
	        assertTrue(stompSession.isConnected());
	
	        stompSession.subscribe(SUBSCRIBE_TOPICS, new CreateListStompFrameHandler());
	        
	        List<?> topics = completableFutureString.get(5, SECONDS);
	        
	        assertNotNull(topics);
		} catch (Exception e) {
			e.printStackTrace();
			fail("fail to retrieve topics.");
		}
    }
	
	private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }
	
	private class CreateChatMessageStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return ChatMessage.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            completableFutureChatMessage.complete((ChatMessage) o);
        }
    }
	
	private class CreateListStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return List.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
        	completableFutureString.complete((List<?>) o);
        }
    }
	
}
