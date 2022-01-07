package com.chat.api.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chat.api.model.ChatMessage;
import com.chat.api.utils.LimitedQueue;

public class ChatDB {
	
	private static ChatDB chatDbInstance = null;

	private static final int MAX_CHAT = 10;
	private Map<Integer, LimitedQueue<ChatMessage>> dbMessages = new HashMap<>();
	private Map<String, Integer> dbUsers = new HashMap<>();
	
	public static ChatDB getInstance() {
		if (chatDbInstance == null)
			chatDbInstance = new ChatDB();

		return chatDbInstance;
	}

	public List<ChatMessage> getChatMessages(int topic) {
		return this.dbMessages.get(topic) != null ? this.dbMessages.get(topic) : new ArrayList<>();
	}

	public int getRanking(final String user) {
		return this.dbUsers.get(user) != null ? this.dbUsers.get(user) : 0;
	}

	public void saveRanking(final ChatMessage chatMessage) {
		this.dbUsers.put(chatMessage.getUser(), chatMessage.getScore());
	}

	public void saveChatMessage(final ChatMessage chatMessage) {
		if (chatMessage.getType().equals(ChatMessage.MessageType.JOIN)
				|| chatMessage.getType().equals(ChatMessage.MessageType.LEAVE)) {
			return;
		}

		if (this.dbMessages.containsKey(chatMessage.getTopicId())) {
			this.dbMessages.get(chatMessage.getTopicId()).add(chatMessage);
		} else {
			LimitedQueue<ChatMessage> chats = new LimitedQueue<>(MAX_CHAT);
			chats.add(chatMessage);
			this.dbMessages.put(chatMessage.getTopicId(), chats);
		}
	}
}
