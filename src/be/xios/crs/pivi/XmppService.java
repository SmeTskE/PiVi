package be.xios.crs.pivi;

import java.util.Date;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import be.xios.crs.pivi.enums.ChatRooms;
import be.xios.crs.pivi.enums.FeedActions;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.managers.DbManager;
import be.xios.crs.pivi.managers.XmppManager;
import be.xios.crs.pivi.models.ChatMessage;
import be.xios.crs.pivi.models.PiviXmppMessage;
import be.xios.crs.pivi.models.Player;
import be.xios.crs.pivi.models.PlayerGameInstance;
import be.xios.crs.pivi.models.User;

public class XmppService extends Service {

	// BROADCAST ACTIONS
	public static final String SERVICE_FEED = "SERVICE_FEED";
	public static final String SERVICE_PIRATE_CHAT = "PIRATE_CHAT";
	public static final String SERVICE_VIKING_CHAT = "VIKING_CHAT";
	public static final String SERVICE_GLOBAL_CHAT = "GLOBAL_CHAT";
	public static final String SERVICE_GPS = "GPS_CHAT";
	// INTENT KEYS
	public static final String GAME_INSTANCE = "GAME_INSTANCE";
	public static final String GPS_MESSAGE = "GPS_MESSAGE";
	public static final String FEED_MESSAGE = "FEED_MESSAGE";
	public static final String PLAYER_JOINED = "PLAYER_JOINED";
	public static final String ORB_COLLECTED = "ORB_COLLECTED";
	public static final String CHAT_MESSAGE = "CHAT_MESSAGE";
	public static final String SERVICE_READY = "SERVICE_READY";
	// INTENT KEYS - ROOMS TO JOIN
	public static final String PAR_JOIN_FEED = "JOIN_FEED";
	public static final String PAR_JOIN_VIKINGS = "JOIN_VIKINGS";
	public static final String PAR_JOIN_PIRATES = "JOIN_PIRATES";
	public static final String PAR_JOIN_GLOBAL = "JOIN_GLOBAL";
	public static final String PAR_JOIN_GPS = "JOIN_GPS";
	public static final String PAR_ORB = "ORB";
	// FEEDS TO BE JOINED
	private boolean JOIN_FEED = false;
	private boolean JOIN_VIKINGS = false;
	private boolean JOIN_PIRATES = false;
	private boolean JOIN_GLOBAL = false;
	private boolean JOIN_GPS = false;
	// CHAT ROOMS
	private MultiUserChat feedChat = null;
	private MultiUserChat gpsChat = null;
	private MultiUserChat vikingChat = null;
	private MultiUserChat piratesChat = null;
	private MultiUserChat globalChat = null;
	// BROADCAST RECEIVERS
	private MyFeedSender feedSender;
	private MyPiratesChatSender piratesChatSender;
	private MyVikingsChatSender vikingsChatSender;
	private MyGlobalChatSender globalChatSender;
	private MyGpsSender gpsSender;
	private MyOrbSender orbSender;
	// BROADCAST ACTIONS
	public static final String FEED_SEND_ACTION = "SEND_FEED";
	public static final String ORB_SEND_ACTION = "ORB_SEND";
	public static final String PIRATES_SEND_ACTION = "SEND_PIRATES";
	public static final String VIKINGS_SEND_ACTION = "SEND_VIKINGS";
	public static final String GLOBAL_SEND_ACTION = "SEND_GLOBAL";
	public static final String GPS_SEND_ACTION = "SEND_GPS";
	// OTHER VARS
	private XMPPConnection xmppConnection;
	private PlayerGameInstance gameInstance;
	private final IBinder mBinder = new LocalBinder();
	private DbManager piviDb;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("XMPP SERVICE", "SERVICE STARTED");
		gameInstance = (PlayerGameInstance) intent.getExtras().getSerializable(
				GAME_INSTANCE);

		piviDb = new DbManager(getApplicationContext());

		JOIN_FEED = intent.getBooleanExtra(PAR_JOIN_FEED, false);
		JOIN_PIRATES = intent.getBooleanExtra(PAR_JOIN_PIRATES, false);
		JOIN_VIKINGS = intent.getBooleanExtra(PAR_JOIN_VIKINGS, false);
		JOIN_GLOBAL = intent.getBooleanExtra(PAR_JOIN_GLOBAL, false);
		JOIN_GPS = intent.getBooleanExtra(PAR_JOIN_GPS, false);

		new Connector(gameInstance).execute();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.d("XMPP SERVICE", "DESTROYING SERVICE");
		new DisConnector().execute();
		if (feedSender != null) {
			unregisterReceiver(feedSender);
			feedSender = null;
		}
		if (orbSender != null) {
			unregisterReceiver(orbSender);
			orbSender = null;
		}
		if (piratesChatSender != null) {
			unregisterReceiver(piratesChatSender);
			piratesChatSender = null;
		}
		if (gpsSender != null) {
			unregisterReceiver(gpsSender);
			gpsSender = null;
		}
		if (vikingsChatSender != null) {
			unregisterReceiver(vikingsChatSender);
			vikingsChatSender = null;
		}
		if (globalChatSender != null) {
			unregisterReceiver(globalChatSender);
			globalChatSender = null;
		}
		if (piviDb != null) {
			piviDb.close();
			piviDb = null;
		}
		super.onDestroy();
	}

	private class DisConnector extends AsyncTask<Object, Integer, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			if (feedChat != null) {
				feedChat.leave();
				feedChat = null;
			}
			if (gpsChat != null) {
				gpsChat.leave();
				gpsChat = null;
			}
			if (vikingChat != null) {
				vikingChat.leave();
				vikingChat = null;
			}
			if (piratesChat != null) {
				piratesChat.leave();
				piratesChat = null;
			}
			if (globalChat != null) {
				globalChat.leave();
				globalChat = null;
			}
			if (xmppConnection != null) {
				xmppConnection.disconnect();
				xmppConnection = null;
			}
			Log.d("XMPP SERVICE", "DISCONNECTED ALL");
			return null;
		}
	}

	private class Connector extends AsyncTask<Object, Integer, Boolean> {

		private PlayerGameInstance gameInstance;

		public Connector(PlayerGameInstance instance) {
			gameInstance = instance;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			boolean result;
			xmppConnection = XmppManager.connect();

			Log.d("XMPP CONNECTION", "CONNECTED TO OPENFIRE");
			XmppManager.logIn(xmppConnection, gameInstance.getPlayer()
					.getUser());

			result = xmppConnection.isConnected();
			if (result) {
				if (JOIN_FEED) {
					feedChat = XmppManager.joinChatGroup(xmppConnection,
							gameInstance.getPlayer().getUser(), gameInstance
									.getServer().getFeedRoom());

					feedSender = new MyFeedSender();
					IntentFilter filter = new IntentFilter();
					filter.addAction(FEED_SEND_ACTION);
					registerReceiver(feedSender, filter);

					orbSender = new MyOrbSender();
					filter = new IntentFilter();
					filter.addAction(ORB_SEND_ACTION);
					registerReceiver(orbSender, filter);
				}
				if (JOIN_GLOBAL) {
					globalChat = XmppManager.joinChatGroup(xmppConnection,
							gameInstance.getPlayer().getUser(), gameInstance
									.getServer().getPublicRoom());

					globalChatSender = new MyGlobalChatSender();
					IntentFilter filter = new IntentFilter();
					filter.addAction(GLOBAL_SEND_ACTION);
					registerReceiver(globalChatSender, filter);
				}
				if (JOIN_PIRATES) {
					piratesChat = XmppManager.joinChatGroup(xmppConnection,
							gameInstance.getPlayer().getUser(), gameInstance
									.getServer().getPrivateRoomPirates());

					piratesChatSender = new MyPiratesChatSender();
					IntentFilter filter = new IntentFilter();
					filter.addAction(PIRATES_SEND_ACTION);
					registerReceiver(piratesChatSender, filter);
				}
				if (JOIN_VIKINGS) {
					vikingChat = XmppManager.joinChatGroup(xmppConnection,
							gameInstance.getPlayer().getUser(), gameInstance
									.getServer().getPrivateRoomVikings());

					vikingsChatSender = new MyVikingsChatSender();
					IntentFilter filter = new IntentFilter();
					filter.addAction(VIKINGS_SEND_ACTION);
					registerReceiver(vikingsChatSender, filter);
				}
				if (JOIN_GPS) {
					gpsChat = XmppManager.joinChatGroup(xmppConnection,
							gameInstance.getPlayer().getUser(), gameInstance
									.getServer().getGpsRoom());

					gpsSender = new MyGpsSender();
					IntentFilter filter = new IntentFilter();
					filter.addAction(GPS_SEND_ACTION);
					registerReceiver(gpsSender, filter);
				}
			}

			if (result) {
				if (feedChat != null && feedChat.isJoined()) {
					feedChat.addMessageListener(new MyFeedListener());
					Log.d("XMPP CHAT", "JOINED FEED CHAT");
				}
				if (globalChat != null && globalChat.isJoined()) {
					globalChat.addMessageListener(new MyGlobalChatListener());
					Log.d("XMPP CHAT", "JOINED GLOBAL CHAT");
				}
				if (piratesChat != null && piratesChat.isJoined()) {
					piratesChat.addMessageListener(new MyPiratesChatListener());
					Log.d("XMPP CHAT", "JOINED PIRATES CHAT");
				}
				if (vikingChat != null && vikingChat.isJoined()) {
					vikingChat.addMessageListener(new MyVikingChatListener());
					Log.d("XMPP CHAT", "JOINED VIKINGS CHAT");
				}
				if (gpsChat != null && gpsChat.isJoined()) {
					gpsChat.addMessageListener(new MyGPSChatListener());
					Log.d("XMPP CHAT", "JOINED GPS CHAT");
				}
			} else {
				Log.d("XMPP CHAT", "COULD NOT CONNECT TO OPENFIRE");
			}

			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				Intent serviceReady = new Intent();
				serviceReady.setAction(SERVICE_READY);
				sendBroadcast(serviceReady);
			}
		}
	}

	/* --- START PACKET LISTENERS --- */
	private class MyFeedListener implements PacketListener {

		@Override
		public void processPacket(Packet packet) {
			Log.d("XMPP SERVICE", "FEED MESSAGE RECEIVED");
			if (packet != null) {
				if (packet instanceof Message) {
					Message in = (Message) packet;
					String type = in.getBody().split("]")[0] + "]";
					if (type.equals(FeedActions.playerJoined.toString())) {
						Log.d("GAME SERVER", "PLAYER JOINED");
						Player newPlayer = new Player();
						User user = new User();
						String info = in.getBody().split("]")[1];
						user.setNickname(info.split(";")[0]);

						if (info.split(";")[1].equals(PlayerTeam.Vikings
								.toString())) {
							newPlayer.setTeam(PlayerTeam.Vikings);
						} else {
							newPlayer.setTeam(PlayerTeam.Pirates);
						}
						newPlayer.setUser(user);

						Intent intent = new Intent();
						intent.setAction(SERVICE_FEED);
						intent.putExtra(FEED_MESSAGE, FeedActions.playerJoined);
						intent.putExtra(PLAYER_JOINED, newPlayer);
						sendBroadcast(intent);

						PiviXmppMessage msg = new PiviXmppMessage();
						msg.setChatRoom(ChatRooms.FeedChat);
						msg.setMessage(in.getBody());
						msg.setMessageSend(new Date());
						msg.setSender(in.getFrom());
						piviDb.insertMessage(msg, gameInstance.getServer()
								.getId());

						Log.d("GAME SERVER", "SEND NEW PLAYER BROADCAST");
					} else if (type.equals(FeedActions.orbCollectedGood
							.toString())) {
						PiviXmppMessage msg = new PiviXmppMessage();
						msg.setChatRoom(ChatRooms.FeedChat);
						msg.setMessage(in.getBody());
						msg.setMessageSend(new Date());
						msg.setSender(in.getFrom());

						Intent intent = new Intent();
						intent.setAction(SERVICE_FEED);
						intent.putExtra(FEED_MESSAGE,
								FeedActions.orbCollectedGood);
						intent.putExtra(ORB_COLLECTED, msg);
						sendBroadcast(intent);
						piviDb.insertMessage(msg, gameInstance.getServer().getId());
					} else if (type.equals(FeedActions.orbCollectedBad
							.toString())) {
						PiviXmppMessage msg = new PiviXmppMessage();
						msg.setChatRoom(ChatRooms.FeedChat);
						msg.setMessage(in.getBody());
						msg.setMessageSend(new Date());
						msg.setSender(in.getFrom());

						Intent intent = new Intent();
						intent.setAction(SERVICE_FEED);
						intent.putExtra(FEED_MESSAGE,
								FeedActions.orbCollectedBad);
						intent.putExtra(ORB_COLLECTED, msg);
						sendBroadcast(intent);
						piviDb.insertMessage(msg, gameInstance.getServer().getId());
					}
				}
			}
		}
	}

	private class MyVikingChatListener implements PacketListener {
		@Override
		public void processPacket(Packet packet) {
			if (packet != null) {
				if (packet instanceof Message) {
					Message in = (Message) packet;
					ChatMessage chatMsg = new ChatMessage();
					PiviXmppMessage message = new PiviXmppMessage();
					message.setSender(in.getFrom());
					message.setMessageSend(new Date());
					message.setMessage(in.getBody().split(";;")[0]);
					chatMsg.setXmppMessage(message);
					chatMsg.setIconId(Integer
							.parseInt(in.getBody().split(";;")[1]));

					Intent intent = new Intent();
					intent.setAction(SERVICE_VIKING_CHAT);
					intent.putExtra(CHAT_MESSAGE, chatMsg);
					sendBroadcast(intent);

					Log.d("GAME SERVER",
							"SEND NEW CHAT MESSAGE VIKINGS BROADCAST");
				}
			}
		}
	}

	private class MyPiratesChatListener implements PacketListener {
		@Override
		public void processPacket(Packet packet) {
			if (packet != null) {
				if (packet instanceof Message) {
					Message in = (Message) packet;
					ChatMessage chatMsg = new ChatMessage();
					PiviXmppMessage message = new PiviXmppMessage();
					message.setSender(in.getFrom());
					message.setMessageSend(new Date());
					message.setMessage(in.getBody().split(";;")[0]);
					chatMsg.setXmppMessage(message);
					chatMsg.setIconId(Integer
							.parseInt(in.getBody().split(";;")[1]));

					Intent intent = new Intent();
					intent.setAction(SERVICE_PIRATE_CHAT);
					intent.putExtra(CHAT_MESSAGE, chatMsg);
					sendBroadcast(intent);

					Log.d("GAME SERVER",
							"SEND NEW CHAT MESSAGE PIRATES BROADCAST");
				}
			}
		}
	}

	private class MyGlobalChatListener implements PacketListener {
		@Override
		public void processPacket(Packet packet) {
			Log.d("XMPP CHAT", "RECEIVED A MESSAGE IN GLOBAL CHAT LISTENER...");
			if (packet != null) {
				if (packet instanceof Message) {
					Message in = (Message) packet;
					ChatMessage chatMsg = new ChatMessage();
					PiviXmppMessage message = new PiviXmppMessage();
					message.setSender(in.getFrom());
					message.setMessageSend(new Date());
					message.setMessage(in.getBody().split(";;")[0]);
					chatMsg.setXmppMessage(message);
					chatMsg.setIconId(Integer
							.parseInt(in.getBody().split(";;")[1]));

					Intent intent = new Intent();
					intent.setAction(SERVICE_GLOBAL_CHAT);
					intent.putExtra(CHAT_MESSAGE, chatMsg);
					sendBroadcast(intent);

					Log.d("GAME SERVER",
							"SEND NEW CHAT MESSAGE GLOBAL BROADCAST");
				}
			}
		}
	}

	private class MyGPSChatListener implements PacketListener {
		@Override
		public void processPacket(Packet packet) {
			if (packet != null) {
				if (packet instanceof Message) {
					Message in = (Message) packet;
					PiviXmppMessage message = new PiviXmppMessage();
					message.setSender(in.getFrom());
					message.setMessage(in.getBody());
					message.setMessageSend(new Date());
					message.setId(0);

					Intent intent = new Intent();
					intent.setAction(SERVICE_GPS);
					intent.putExtra(GPS_MESSAGE, message);
					sendBroadcast(intent);
					Log.d("XMPP GPS SERVICE",
							"GPS DATA RECEIVED AND BROADCASTED");
				}
			}
		}
	}

	/* --- END PACKET LISTENERS --- */

	/* --- START BROADCAST RECEIVERS --- */
	private class MyFeedSender extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (feedChat != null) {
					feedChat.sendMessage(FeedActions.playerJoined
							+ gameInstance.getPlayer().getUser().getNickname()
							+ ";"
							+ gameInstance.getPlayer().getTeam().toString());
				}
			} catch (XMPPException ex) {
				Log.d("XMPP CHAT",
						"EXCEPTION OCCURED WHILE TRYING TO JOIN CHAT, message: "
								+ ex.getMessage());
				feedChat = null;
			}
		}
	}

	private class MyPiratesChatSender extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ChatMessage msg = (ChatMessage) intent
					.getSerializableExtra(PIRATES_SEND_ACTION);
			Message xmppMessage = new Message();
			xmppMessage.setBody(msg.getXmppMessage().getMessage() + ";;"
					+ msg.getIconId());
			try {
				piratesChat.sendMessage(xmppMessage);
			} catch (XMPPException e) {
				Log.d("XMPP CONNECTION",
						"COULD NOT SEND MESSAGE TO PIRATE CHAT, message: "
								+ e.getMessage());
			}
			;
		}
	}

	private class MyVikingsChatSender extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ChatMessage msg = (ChatMessage) intent
					.getSerializableExtra(VIKINGS_SEND_ACTION);
			Message xmppMessage = new Message();
			xmppMessage.setBody(msg.getXmppMessage().getMessage() + ";;"
					+ msg.getIconId());
			try {
				vikingChat.sendMessage(xmppMessage);
			} catch (XMPPException e) {
				Log.d("XMPP CONNECTION",
						"COULD NOT SEND MESSAGE TO VIKING CHAT, message: "
								+ e.getMessage());
			}
		}
	}

	private class MyGlobalChatSender extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("XMPP CHAT",
					"RECEIVED BROADCAST TO SEND GLOBAL CHAT MESSAGE...");
			ChatMessage msg = (ChatMessage) intent
					.getSerializableExtra(GLOBAL_SEND_ACTION);
			try {
				globalChat.sendMessage(msg.getXmppMessage().getMessage() + ";;"
						+ msg.getIconId());
				Log.d("XMPP CHAT", "MESSAGE SHOULD BE SEND...");
			} catch (XMPPException e) {
				Log.d("XMPP CONNECTION",
						"COULD NOT SEND MESSAGE TO GLOBAL CHAT, message: "
								+ e.getMessage());
			}
			;
		}
	}

	private class MyGpsSender extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("XMPP SERVICE", "GOING TO SEND GPS DATA");
			PiviXmppMessage msg = (PiviXmppMessage) intent
					.getSerializableExtra(XmppService.GPS_MESSAGE);
			try {
				gpsChat.sendMessage(msg.getMessage());
				Log.d("XMPP GPS", "GPS DATA SHOULD BE SEND...");
			} catch (XMPPException e) {
				Log.d("XMPP CONNECTION",
						"COULD NOT SEND MESSAGE TO GPS CHAT, message: "
								+ e.getMessage());
			}

		}
	}

	private class MyOrbSender extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			PiviXmppMessage msg = (PiviXmppMessage) intent
					.getSerializableExtra(PAR_ORB);
			try {
				feedChat.sendMessage(msg.getMessage());
				Log.d("XMPP ORBS", "ORB SHOULD BE SENDED TO FEEDCHATROOM");
			} catch (XMPPException e) {
				Log.d("XMPP ORBS",
						"COULD NOT SEND ORB, message: " + e.getMessage());
			}
		}
	}

	/* --- END BROADCAST RECEIVERS --- */

	public class LocalBinder extends Binder {
		XmppService getService() {
			return XmppService.this;
		}
	}
}
