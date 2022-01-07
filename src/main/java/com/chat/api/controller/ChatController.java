package com.chat.api.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.chat.api.model.ChatMessage;
import com.chat.api.repository.ChatDB;
import com.chat.api.service.RankingService;


@Controller
public class ChatController {

	@MessageMapping("/chat/register/{topicId}")
	@SendTo("/topic/chat/sended/{topicId}")
	public ChatMessage register(@DestinationVariable() final Integer topicId, @Payload final ChatMessage chatMessage,
			final SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", chatMessage.getUser());
		return chatMessage;
	}

	@MessageMapping("/chat/send/{topicId}")
	@SendTo("/topic/chat/sended/{topicId}")
	public ChatMessage sendMessage(@DestinationVariable final Integer topicId, @Payload final ChatMessage chatMessage) {
		new RankingService(chatMessage).run();
		return chatMessage;
	}

	@SubscribeMapping("/chat/topics/subscribe")
	public List<String> topics() {
		return Arrays.asList("Public", "EA", "Gaming", "Crypto");
	}

	@SubscribeMapping("/chat/messages/subscribe/{topicId}")
	public List<ChatMessage> chatMessages(@DestinationVariable final Integer topicId) {
		return ChatDB.getInstance().getChatMessages(topicId);
	}
	
	@MessageMapping("/chat/test")
	@SendTo("/topic/chat/tested")
	public String register(@Payload final String chatMessage, final SimpMessageHeaderAccessor headerAccessor) {
		return "hello";
	}

}
