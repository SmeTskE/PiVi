package be.xios.crs.pivi.models;

import java.io.Serializable;

public class ChatMessage implements Serializable {

	private static final long serialVersionUID = 7606616882664950292L;
	private PiviXmppMessage xmppMessage;
	private int iconId;
	
	public PiviXmppMessage getXmppMessage() {
		return xmppMessage;
	}
	public void setXmppMessage(PiviXmppMessage xmppMessage) {
		this.xmppMessage = xmppMessage;
	}
	public int getIconId() {
		return iconId;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}	
}