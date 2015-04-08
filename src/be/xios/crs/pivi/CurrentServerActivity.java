package be.xios.crs.pivi;

import java.util.ArrayList;
import org.jivesoftware.smack.SmackAndroid;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import be.xios.crs.pivi.XmppService.LocalBinder;
import be.xios.crs.pivi.adapters.PlayerListAdapter;
import be.xios.crs.pivi.enums.FeedActions;
import be.xios.crs.pivi.managers.WebManager;
import be.xios.crs.pivi.models.Player;
import be.xios.crs.pivi.models.PlayerGameInstance;

public class CurrentServerActivity extends ListActivity implements
		OnClickListener {

	public static final String INTENT_SERVER_INFORMATION = "SERVER_INFORMATION";
	private Button btnStart;
	private PlayerGameInstance gameInstance;
	private ArrayList<Player> playersList;
	private ProgressDialog progressDialog;
	private PlayerListAdapter adapter;
	private MyServiceReceiver receiver;
	private MyServiceChecker serviceReadyReceiver;
	XmppService mService;
	boolean mBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_server);

		btnStart = (Button) findViewById(R.id.currentserver_start);
		btnStart.setOnClickListener(this);

		gameInstance = (PlayerGameInstance) getIntent().getSerializableExtra(
				INTENT_SERVER_INFORMATION);

		playersList = new ArrayList<Player>();

		adapter = new PlayerListAdapter(getApplicationContext(),
				R.layout.player_list_item, playersList);
		setListAdapter(adapter);

		setTitle(gameInstance.getServer().getNaam());

		SmackAndroid.init(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		receiver = new MyServiceReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_FEED);
		registerReceiver(receiver, intentFilter);

		serviceReadyReceiver = new MyServiceChecker();
		intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_READY);
		registerReceiver(serviceReadyReceiver, intentFilter);

		Intent intent = new Intent(this, XmppService.class);
		intent.putExtra(XmppService.GAME_INSTANCE, gameInstance);
		intent.putExtra(XmppService.PAR_JOIN_FEED, true);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		startService(intent);
	}

	@Override
	protected void onStop() {
		unregisterReceiver(receiver);
		unregisterReceiver(serviceReadyReceiver);
		unbindService(mConnection);
		mBound = false;
		stopService(new Intent(this,XmppService.class));
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.currentserver_start:
			startGame();
			break;
		}
	}

	private void startGame() {
		Intent i = new Intent(getApplicationContext(), GameMapActivity.class);
		i.putExtra(GameMapActivity.INTENT_SERVER_INFORMATION, gameInstance);
		startActivity(i);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	@Override
	public void onBackPressed() {
		WebManager.deletePlayer(gameInstance.getPlayer().getId());
		super.onBackPressed();
	}

	private class MyServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getSerializableExtra(XmppService.FEED_MESSAGE) != null) {
				FeedActions type = (FeedActions) intent.getSerializableExtra(XmppService.FEED_MESSAGE);

				if (type == FeedActions.playerJoined) {
					Player player = (Player) intent.getExtras()
							.getSerializable(XmppService.PLAYER_JOINED);
					adapter.add(player);
					adapter.notifyDataSetChanged();
				}
			}
		}
	}

	private class MyServiceChecker extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Intent intent = new Intent();
			intent.setAction(XmppService.FEED_SEND_ACTION);
			Log.d("XMPP MESSAGES", "SEND PLAYER JOINED");
			sendBroadcast(intent);
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
}
