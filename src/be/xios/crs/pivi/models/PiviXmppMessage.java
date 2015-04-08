package be.xios.crs.pivi.models;

import java.io.Serializable;
import java.util.Date;

import be.xios.crs.pivi.enums.ChatRooms;

public class PiviXmppMessage implements Serializable {

	private static final long serialVersionUID = 176909747627540782L;
	private long id;
	private String sender;
	private String message;
	private Date messageSend;
	private ChatRooms chatRoom;
	
	public ChatRooms getChatRoom() {
		return chatRoom;
	}
	public void setChatRoom(ChatRooms chatRoom) {
		this.chatRoom = chatRoom;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getMessageSend() {
		return messageSend;
	}
	public void setMessageSend(Date messageSend) {
		this.messageSend = messageSend;
	}
}
