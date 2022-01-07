package com.chat.api.model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String content;
	private String user;
	private MessageType type;
	private int score;
	private int topicId;
	
	public ChatMessage() {
	}
	
	public ChatMessage(String content, String user, MessageType type, int score, int topicId) {
		super();
		this.content = content;
		this.user = user;
		this.type = type;
		this.score = score;
		this.topicId = topicId;
	}

	public enum MessageType {
		CHAT, LEAVE, JOIN
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the type
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MessageType type) {
		this.type = type;
	}

	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * @return the topicId
	 */
	public int getTopicId() {
		return topicId;
	}

	/**
	 * @param topicId the topicId to set
	 */
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	
	
}
