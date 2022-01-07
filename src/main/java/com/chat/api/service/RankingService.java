package com.chat.api.service;

import com.chat.api.model.ChatMessage;
import com.chat.api.repository.ChatDB;
import com.ea.chat.score.ScorerService;
import com.ea.chat.score.exceptions.ServiceUnavailableException;

public class RankingService implements Runnable {

	private ChatMessage chatMessage;

	public RankingService(ChatMessage chatMessage) {
		this.chatMessage = chatMessage;
	}
	
	@Override
	public void run() {
		int messageScored = this.scoreMessage();
		
		int newScore = this.addScore(messageScored);
		
		this.chatMessage.setScore(newScore);

		ChatDB.getInstance().saveChatMessage(chatMessage);
		ChatDB.getInstance().saveRanking(chatMessage); 
	}

	private int addScore(int messageScored) {
		int oldRanking = ChatDB.getInstance().getRanking(chatMessage.getUser()); 
		return oldRanking + messageScored;
	}
	
	private int scoreMessage() {
		try {
			final ScorerService scoreService = new ScorerService();
			return scoreService.getScorer().score(this.chatMessage.getContent());
		} catch (ServiceUnavailableException e) {
			return 0;
		}
	}

}
