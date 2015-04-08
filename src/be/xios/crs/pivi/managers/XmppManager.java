package be.xios.crs.pivi.managers;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import android.util.Log;
import be.xios.crs.pivi.models.User;

public class XmppManager {

	private static String xmppHostAdress = "smetske.mine.nu";
	private static String xmppConferenceLocation = "@conference.localhost";

	/**
	 * Method to connect to XMPP server Warning: needs to run in AsyncTask!
	 * 
	 * @return XMPPConnection
	 */
	public static XMPPConnection connect() {
		XMPPConnection connector = null;
		try {
			SASLAuthentication.supportSASLMechanism("PLAIN");
			ConnectionConfiguration configuration = new ConnectionConfiguration(
					xmppHostAdress);

			connector = new XMPPConnection(configuration);
			Log.d("XMPP CONNECTION", "Trying to connect...");
			connector.connect();

			Log.d("XMPP CONNECTION", "Connected to " + connector.getHost());

		} catch (XMPPException e) {
			Log.d("XMPP", "Could not connect: " + e.getMessage());
			e.printStackTrace();
		}
		return connector;
	}

	/**
	 * Method to create an account on the XMPP server Warning: Uses connection
	 * parameter by reference!
	 * 
	 * @param connection
	 * @param user
	 * @return successfull or not
	 */
	public static boolean createAccount(XMPPConnection connection, User user) {
		try {
			connection.getAccountManager().createAccount(user.getUsername(),
					user.getPassword());
			Log.d("XMPP CONNECTION", "ACCOUNT CREATED");
			return true;
		} catch (XMPPException e) {
			Log.d("XMPP CONNECTION",
					"COULD NOT CREATE ACCOUNT, message:" + e.getMessage());
			return false;
		}
	}

	/**
	 * Method to create an account on the XMPP server
	 * 
	 * @param user
	 * @return successfull or not
	 */
	public static Boolean createAccount(User user) {
		XMPPConnection connection = connect();
		boolean result = createAccount(connection, user);
		connection.disconnect();
		return result;
	}

	/**
	 * 
	 * Method to log a user in Warning: Uses connection parameter by reference!
	 * 
	 * @param connection
	 * @param user
	 * @return successfull or not
	 */
	public static boolean logIn(XMPPConnection connection, User user) {
		try {
			connection.login(user.getUsername(), user.getPassword());
			Log.d("XMPP CONNECTION", "LOGGED IN AS " + connection.getUser());
			return true;
		} catch (XMPPException e) {
			Log.d("XMPP CONNECTION",
					"Could not login to server, message: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Method to create or join a chatroom
	 * 
	 * @param connection
	 * @param user
	 * @param roomName
	 * @return
	 */
	public static MultiUserChat createChatGroup(XMPPConnection connection,
			User user, String roomName) {
		MultiUserChat chat = null;
		if (connection != null & user != null & !roomName.equals("")) {
			chat = new MultiUserChat(connection, roomName
					+ xmppConferenceLocation);
			try {
				chat.create("testbot");

				chat.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
				chat.leave();

			} catch (XMPPException ex) {
				Log.d("XMPP CHAT", "Could not create/join chat " + roomName
						+ ", message " + ex.getMessage());
				chat = null;
			}
		}
		return chat;
	}

	public static MultiUserChat joinChatGroup(XMPPConnection connection,
			User user, String roomName) {
		MultiUserChat chat = null;
		if (connection != null & user != null & !roomName.equals("")) {
			chat = new MultiUserChat(connection, roomName
					+ xmppConferenceLocation);
			try {
				DiscussionHistory history = new DiscussionHistory();
				history.setMaxStanzas(Integer.MAX_VALUE);
				chat.join(user.getNickname(),"", history, SmackConfiguration.getPacketReplyTimeout());

			} catch (XMPPException ex) {
				Log.d("XMPP CHAT", "Could not create/join chat " + roomName
						+ ", message " + ex.getMessage());
				chat = null;
			}
		}
		return chat;
	}

	/**
	 * Method to disconnect the connection Warning! uses the connection by
	 * reference
	 * 
	 * @param connection
	 */
	public static void disconnect(XMPPConnection connection) {
		connection.disconnect();
	}
}
