package be.xios.crs.pivi.enums;

public enum ChatRooms {
	GlobalChat("GLOBAL_CHAT"), PiratesChat("PIRATES_CHAT"), VikingsChat(
			"VIKINGS_CHAT"), GpsChat("GPS_CHAT"), FeedChat("FEED_CHAT");

	private String room;

	private ChatRooms(String pRoom) {
		room = pRoom;
	}

	@Override
	public String toString() {
		return room;
	}

	public static ChatRooms byString(String chatRoom) {
		if (chatRoom.equals(ChatRooms.FeedChat.toString())) {
			return ChatRooms.FeedChat;
		} else if (chatRoom.equals(ChatRooms.GlobalChat.toString())) {
			return ChatRooms.GlobalChat;
		} else if (chatRoom.equals(ChatRooms.GpsChat.toString())) {
			return ChatRooms.GpsChat;
		} else if (chatRoom.equals(ChatRooms.PiratesChat.toString())) {
			return ChatRooms.PiratesChat;
		} else {
			return ChatRooms.VikingsChat;
		}
	}
}
