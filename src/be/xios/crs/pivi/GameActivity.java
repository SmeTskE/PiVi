package be.xios.crs.pivi;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.SmackAndroid;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import be.xios.crs.pivi.XmppService.LocalBinder;
import be.xios.crs.pivi.adapters.ChatListAdapter;
import be.xios.crs.pivi.adapters.FeedAdapter;
import be.xios.crs.pivi.enums.ChatRooms;
import be.xios.crs.pivi.enums.FeedActions;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.fragments.GameChatFragment;
import be.xios.crs.pivi.fragments.GameFeedFragment;
import be.xios.crs.pivi.fragments.GameScoreFragment;
import be.xios.crs.pivi.managers.DbManager;
import be.xios.crs.pivi.managers.WebManager;
import be.xios.crs.pivi.models.ChatMessage;
import be.xios.crs.pivi.models.PiviXmppMessage;
import be.xios.crs.pivi.models.Player;
import be.xios.crs.pivi.models.PlayerGameInstance;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;

public class GameActivity extends SherlockFragmentActivity {
	
	// ARRAY ADAPTERS
	private List<PiviXmppMessage> feedList;
	private FeedAdapter feedArrayAdapter;
	private List<ChatMessage> vikingsChatList;
	private ChatListAdapter vikingsAdapter;
	private List<ChatMessage> piratesChatList;
	private ChatListAdapter piratesAdapter;
	private List<ChatMessage> globalChatList;
	private ChatListAdapter globalAdapter;
	// BROADCAST RECEIVERS
	private MyFeedReceiver feedReceiver;
	private MyChatGlobalReceiver globalReceiver;
	private MyChatPiratesReceiver piratesReceiver;
	private MyChatVikingsReceiver vikingsReceiver;
	private MyServiceChecker serviceChecker;
	// OTHER VARS
	private static final String TAG_FRAGMENT_SCOREBOARD = "tag_frag_scoreboard";
	public static final String INTENT_SERVER_INFORMATION = "SERVER_INFORMATION"; 
	private PlayerGameInstance gameInstance;	
	private FragmentTabHost mTabHost;
	XmppService mService;
	boolean mBound = false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_game_tabs);

		Context context = this;
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayShowTitleEnabled(true);
		
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		
		mTabHost.addTab(mTabHost.newTabSpec("chat").setIndicator("Chat"),
				GameChatFragment.class, null);
		
		mTabHost.addTab(mTabHost.newTabSpec("feed").setIndicator("Feed"),
				GameFeedFragment.class, null);
		
		mTabHost.addTab(mTabHost.newTabSpec("score").setIndicator("Score"),
				GameScoreFragment.class, null);
		
		gameInstance = (PlayerGameInstance) getIntent().getSerializableExtra(INTENT_SERVER_INFORMATION);
		
		// INITIALISE XMPP ROOMS
		DbManager db = new DbManager(this);
		
		feedList = db.getAllMessageByRoom(ChatRooms.FeedChat, gameInstance.getServer().getId());
		feedArrayAdapter = new FeedAdapter(context, android.R.layout.simple_list_item_1, 
				android.R.id.text1, feedList);	
		
		globalChatList = new ArrayList<ChatMessage>();
		globalAdapter = new ChatListAdapter(context, R.layout.list_item_chat, globalChatList);
		
		if (gameInstance.getPlayer().getTeam() == PlayerTeam.Pirates){
			piratesChatList = new ArrayList<ChatMessage>();
			piratesAdapter = new ChatListAdapter(context, R.layout.list_item_chat, piratesChatList);
		}else{
			vikingsChatList = new ArrayList<ChatMessage>();
			vikingsAdapter = new ChatListAdapter(context, R.layout.list_item_chat, vikingsChatList);
		}
		
		SmackAndroid.init(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		serviceChecker = new MyServiceChecker();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_READY);
		registerReceiver(serviceChecker, intentFilter);
		
		feedReceiver = new MyFeedReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_FEED);
		registerReceiver(feedReceiver, intentFilter);
		
		intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_GLOBAL_CHAT);
		
		globalReceiver = new MyChatGlobalReceiver();
		registerReceiver(globalReceiver, intentFilter);
		
		Intent intent = new Intent(this, XmppService.class);
		intent.putExtra(XmppService.GAME_INSTANCE, gameInstance);
		intent.putExtra(XmppService.PAR_JOIN_FEED, true);
		intent.putExtra(XmppService.PAR_JOIN_GLOBAL, true);
		if (gameInstance.getPlayer().getTeam() == PlayerTeam.Pirates){
			//intent.putExtra(XmppService.PAR_JOIN_PIRATES, true);
			piratesReceiver = new MyChatPiratesReceiver();
			intentFilter = new IntentFilter();
			intentFilter.addAction(XmppService.SERVICE_PIRATE_CHAT);
			registerReceiver(piratesReceiver, intentFilter);
		}else{
			//intent.putExtra(XmppService.PAR_JOIN_VIKINGS, true);
			vikingsReceiver = new MyChatVikingsReceiver();
			intentFilter = new IntentFilter();
			intentFilter.addAction(XmppService.SERVICE_VIKING_CHAT);
			registerReceiver(vikingsReceiver, intentFilter);
		}
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		startService(intent);
	}
	
	@Override
	protected void onStop() {
		unregisterReceiver(feedReceiver);
		unregisterReceiver(globalReceiver);
		if (piratesReceiver != null){
			unregisterReceiver(piratesReceiver);
			piratesReceiver = null;
		}
		if (vikingsReceiver != null){
			unregisterReceiver(vikingsReceiver);
			vikingsReceiver = null;
		}
		if (serviceChecker != null){
			unregisterReceiver(serviceChecker);
			serviceChecker = null;
		}
		unbindService(mConnection);
		mBound = false;
		stopService(new Intent(this,XmppService.class));
		super.onStop();
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_game, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		if (item.getItemId() == R.id.items_map) {
			Intent i = new Intent(getApplicationContext(), GameMapActivity.class);
			i.putExtra(GameMapActivity.INTENT_SERVER_INFORMATION, gameInstance);
			i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);			
		}
		
		return super.onOptionsItemSelected(item);
	}

	private class MyFeedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("XMPP CHAT", "RECEIVED FEED MESSAGE FOR FRAGMENT");
			if (intent.getSerializableExtra(XmppService.FEED_MESSAGE) != null) {
				FeedActions type = (FeedActions) intent.getSerializableExtra(XmppService.FEED_MESSAGE);

				if (type == FeedActions.playerJoined) {
					Player player = (Player) intent.getExtras()
							.getSerializable(XmppService.PLAYER_JOINED);
					PiviXmppMessage msg = new PiviXmppMessage();
					msg.setMessage(player.getUser().getNickname() + " joined.");
					feedArrayAdapter.add(msg);
					feedArrayAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	private class MyChatPiratesReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			ChatMessage message = (ChatMessage) intent.getExtras().get(XmppService.CHAT_MESSAGE);
			piratesAdapter.add(message);
			Log.d("XMPP CHAT", "ADDED CHAT MESSAGE TO ARRAYADAPTER PIRATES");
			piratesAdapter.notifyDataSetChanged();
		}		
	}
	
	private class MyChatVikingsReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			ChatMessage message = (ChatMessage) intent.getExtras().get(XmppService.CHAT_MESSAGE);
			vikingsAdapter.add(message);
			Log.d("XMPP CHAT", "ADDED CHAT MESSAGE TO ARRAYADAPTER VIKINGS");
			vikingsAdapter.notifyDataSetChanged();
		}		
	}
	
	private class MyChatGlobalReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			ChatMessage message = (ChatMessage) intent.getExtras().get(XmppService.CHAT_MESSAGE);
			globalAdapter.add(message);
			Log.d("XMPP CHAT", "ADDED CHAT MESSAGE TO ARRAYADAPTER GLOBAL");
			globalAdapter.notifyDataSetChanged();
		}		
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	public FeedAdapter getFeedAdapter() {
		return feedArrayAdapter;
	}
	
	public ChatListAdapter getPiratesAdapter(){
		return piratesAdapter;
	}
	
	public ChatListAdapter getVikingsAdapter(){
		return vikingsAdapter;
	}
	
	public ChatListAdapter getGlobalAdapter(){
		return globalAdapter;
	}
	
	public void SendChatMessage(ChatMessage chatMessage, ChatRooms chatRoom){
		Log.d("XMPP CHAT", "GOING TO SEND CHAT MESSAGE...");
		Intent intent = new Intent();
		switch (chatRoom){
		case GlobalChat:			
			intent.setAction(XmppService.GLOBAL_SEND_ACTION);
			intent.putExtra(XmppService.GLOBAL_SEND_ACTION, chatMessage);
			break;
		case VikingsChat:
			intent.setAction(XmppService.VIKINGS_SEND_ACTION);
			intent.putExtra(XmppService.VIKINGS_SEND_ACTION, chatMessage);			
			break;
		case PiratesChat:
			intent.setAction(XmppService.PIRATES_SEND_ACTION);
			intent.putExtra(XmppService.PIRATES_SEND_ACTION, chatMessage);		
			break;
		}
		sendBroadcast(intent);
	}

	public PlayerGameInstance getGameInstance() {
		return gameInstance;
	}	
	
	private class MyServiceChecker extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			GameChatFragment frag = 
					(GameChatFragment) getSupportFragmentManager().findFragmentById(R.id.frag_chat);
			//frag.btnSend.setEnabled(true);
		}
	}
}
