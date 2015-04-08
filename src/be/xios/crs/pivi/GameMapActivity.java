package be.xios.crs.pivi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jivesoftware.smack.SmackAndroid;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import be.xios.crs.pivi.XmppService.LocalBinder;
import be.xios.crs.pivi.enums.ChatRooms;
import be.xios.crs.pivi.enums.FeedActions;
import be.xios.crs.pivi.enums.OrbType;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.listeners.PiViLocationOverlay;
import be.xios.crs.pivi.managers.DbManager;
import be.xios.crs.pivi.managers.WebManager;
import be.xios.crs.pivi.maps.PiViOverlay;
import be.xios.crs.pivi.maps.PiViOverlayItem;
import be.xios.crs.pivi.models.Orb;
import be.xios.crs.pivi.models.PiviXmppMessage;
import be.xios.crs.pivi.models.PlayerGameInstance;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class GameMapActivity extends SherlockMapActivity {

	private PlayerGameInstance gameInstance;
	public static final String INTENT_SERVER_INFORMATION = "SERVER_INFORMATION";

	private PiViLocationOverlay mLocationOverlay;
	private MapView mMapView;
	private MapController mController;
	protected static boolean mXmppServiceStarted = false;
	protected static List<Intent> mMessageQueue;

	private PiViOverlay mOverlayGoodPlayers;
	private PiViOverlay mOverlayBadPlayers;
	private PiViOverlay mOverlayGoodOrbs;
	private PiViOverlay mOverlayBadOrbs;

	/** HASSELT */
	/*
	 * private static final int minLat = 50926600; private static final int
	 * minLng = 5332403; private static final int maxLat = 50934443; private
	 * static final int maxLng = 5344076;
	 */

	/** XIOS **/
	 private static final int minLat = 50927171;
	 private static final int minLng =  5384244; 
	 private static final int maxLat = 50928436; 
	 private static final int maxLng =  5386347;

	/** DEBUG **/

	/*
	private static final int minLat = 50906017;
	private static final int minLng = 5429272;
	private static final int maxLat = 50908405;
	private static final int maxLng = 5431214;
	*/

	// --- XMPP VARS --- //
	private XmppService mService;
	private boolean mBound = false;
	private MyGpsReceiver gpsReceiver;
	private MyFeedReceiver feedReceiver;
	private MyServiceChecker serviceReadyReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_map);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);

		gameInstance = (PlayerGameInstance) getIntent().getSerializableExtra(
				INTENT_SERVER_INFORMATION);

		mMessageQueue = new ArrayList<Intent>();

		initMap();

		DbManager man = new DbManager(this);
		List<PiviXmppMessage> msgs = man.getAllMessageByRoom(ChatRooms.FeedChat, gameInstance.getServer().getId());
		
		for (PiviXmppMessage msg : msgs){
			this.updateOrbLocations(msg.getMessage());
		}
		
		SmackAndroid.init(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocationOverlay.disableMyLocation();
	}

	@Override
	protected void onStart() {
		super.onStart();

		gpsReceiver = new MyGpsReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_GPS);
		registerReceiver(gpsReceiver, intentFilter);

		feedReceiver = new MyFeedReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_FEED);
		registerReceiver(feedReceiver, intentFilter);

		serviceReadyReceiver = new MyServiceChecker();
		intentFilter = new IntentFilter();
		intentFilter.addAction(XmppService.SERVICE_READY);
		registerReceiver(serviceReadyReceiver, intentFilter);

		Intent intent = new Intent(this, XmppService.class);
		intent.putExtra(XmppService.GAME_INSTANCE, gameInstance);
		intent.putExtra(XmppService.PAR_JOIN_GPS, true);
		intent.putExtra(XmppService.PAR_JOIN_FEED, true);
		startService(intent);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		if (gpsReceiver != null) {
			unregisterReceiver(gpsReceiver);
			gpsReceiver = null;
		}
		if (feedReceiver != null) {
			unregisterReceiver(feedReceiver);
			feedReceiver = null;
		}
		if (serviceReadyReceiver != null) {
			unregisterReceiver(serviceReadyReceiver);
			serviceReadyReceiver = null;
		}
		unbindService(mConnection);
		mBound = false;
		stopService(new Intent(this, XmppService.class));
		super.onStop();
	};

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_gamemap, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_details) {
			Intent i = new Intent(getApplicationContext(), GameActivity.class);
			i.putExtra(GameActivity.INTENT_SERVER_INFORMATION, gameInstance);
			i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

	private void initMap() {
		mMapView = (MapView) findViewById(R.id.mapview);
		mController = mMapView.getController();

		mLocationOverlay = new PiViLocationOverlay(GameMapActivity.this,
				mMapView, gameInstance);

		mController.setZoom(20);
		// TODO: Remove next line for final
		mController.setCenter(new GeoPoint(((int) (50.90737 * 1E6)),
				((int) (5.430205 * 1E6))));
		/*
		 * mLocationOverlay.runOnFirstFix(new Runnable() {
		 * 
		 * @Override public void run() {
		 * Log.i(GameMapActivity.class.getSimpleName(), "OnFirstFix()");
		 * mController.setCenter(mLocationOverlay.getMyLocation()); } });
		 */

		// Init overlays
		Drawable marker;
		// Good Orbs
		marker = getResources().getDrawable(R.drawable.orb_viking);
		mOverlayGoodOrbs = new PiViOverlay(marker);
		mLocationOverlay.setOverlayGoodOrbs(mOverlayGoodOrbs);

		// Bad Orbs
		marker = getResources().getDrawable(R.drawable.orb_pirate);
		mOverlayBadOrbs = new PiViOverlay(marker);
		mLocationOverlay.setOverlayBadOrbs(mOverlayBadOrbs);

		// Good Players
		marker = getResources().getDrawable(R.drawable.icon_user_viking);
		mOverlayGoodPlayers = new PiViOverlay(marker);

		marker = getResources().getDrawable(R.drawable.icon_user_pirate);
		mOverlayBadPlayers = new PiViOverlay(marker);

		// Add Overlays
		mMapView.getOverlays().add(mLocationOverlay);

		mMapView.getOverlays().add(mOverlayGoodOrbs);
		mMapView.getOverlays().add(mOverlayBadOrbs);
		mMapView.getOverlays().add(mOverlayGoodPlayers);
		mMapView.getOverlays().add(mOverlayBadPlayers);

		// Generate Orbs
		if (gameInstance.getOwner()) {
			generateOrbs();
		}

	}

	private List<Orb> generateOrbs() {
		List<Orb> orbs = new ArrayList<Orb>();
		Orb orb;
		PiViOverlayItem orbOverlay;

		for (int i = 0; i < 32; i++) {
			if (i < 24) {
				orb = this.generateOrb(OrbType.GoodOrb);
				// Moved to broadcast receiver
				// orbOverlay = new PiViOverlayItem(orb.getPoint(), "Good orb",
				// "");
				// mOverlayGoodOrbs.addOverlay(orbOverlay);
			} else {
				orb = this.generateOrb(OrbType.BadOrb);
				// Moved to broadcast receiver
				// orbOverlay = new PiViOverlayItem(orb.getPoint(), "Bad orb",
				// "");
				// mOverlayBadOrbs.addOverlay(orbOverlay);
			}
		}

		return orbs;
	}

	private Orb generateOrb(OrbType type) {
		Random rnd = new Random();
		int rndLat = rnd.nextInt(maxLat - minLat) + minLat;
		int rndLng = rnd.nextInt(maxLng - minLng) + minLng;

		Orb orb = new Orb();

		orb.setLatitude(rndLat);
		orb.setLongitude(rndLng);
		orb.setOrbType(type);

		/*
		 * HOTFIX PiviXmppMessage msg = new PiviXmppMessage(); String orbType =
		 * ""; if (type == OrbType.BadOrb) { orbType =
		 * FeedActions.orbSpawnedBad.toString(); } else if (type ==
		 * OrbType.GoodOrb) { orbType = FeedActions.orbSpawnedGood.toString(); }
		 */
		PiviXmppMessage msg = new PiviXmppMessage();
		String orbTypeStr = "";
		String realOrbTypeStr = "";

		if (type == OrbType.BadOrb) {
			orbTypeStr = FeedActions.orbCollectedBad.toString();
			realOrbTypeStr = FeedActions.orbSpawnedBad.toString();
		} else if (type == OrbType.GoodOrb) {
			orbTypeStr = FeedActions.orbCollectedGood.toString();
			realOrbTypeStr = FeedActions.orbSpawnedGood.toString();
		}

		msg.setMessage(orbTypeStr + ";;" + orb.getLatitude() + ";;"
				+ orb.getLongitude() + ";;" + realOrbTypeStr);

		sendFeed(msg);

		return orb;
	}

	@Override
	public void onBackPressed() {

		WebManager.deletePlayer(gameInstance.getPlayer().getId());

		Intent intent = new Intent(getApplicationContext(),
				ServersActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

		// super.onBackPressed();
	}

	private void updateOrbLocations(String message) {
		String[] msg = message.split(";;");
		int lat;
		int lng;
		GeoPoint p;
		String orbTypeStr;

		if (msg.length == 4) {
			String fake = msg[0];
			lat = Integer.parseInt(msg[1]);
			lng = Integer.parseInt(msg[2]);
			orbTypeStr = msg[3];

			p = new GeoPoint(lat, lng);

			if (orbTypeStr.equals(FeedActions.orbCollectedBad.toString())) {
				this.removeBadOrb(p);
			} else if (orbTypeStr.equals(FeedActions.orbCollectedGood
					.toString())) {
				this.removeGoodOrb(p);
			} else if (orbTypeStr.equals(FeedActions.orbSpawnedBad.toString())) {
				this.createBadOrb(p);
			} else if (orbTypeStr.equals(FeedActions.orbSpawnedGood.toString())) {
				this.createGoodOrb(p);
			}

		}
	}

	private void createGoodOrb(GeoPoint p) {
		PiViOverlayItem item = new PiViOverlayItem(p,
				OrbType.GoodOrb.toString(), "");
		mOverlayGoodOrbs.addOverlay(item);
	}

	private void createBadOrb(GeoPoint p) {
		PiViOverlayItem item = new PiViOverlayItem(p,
				OrbType.BadOrb.toString(), "");
		mOverlayBadOrbs.addOverlay(item);
	}

	private void removeGoodOrb(GeoPoint p) {
		// Remove orb from overlay (for all players)

		// Generate new orb (only for creator);
	}

	private void removeBadOrb(GeoPoint p) {
		// Remove orb from overlay (for all players)

		// Generate new orb (only for creator);
	}

	private void updatePlayerLocations(String sender, String message) {

		String[] msg = message.split(";;");
		int lat;
		int lng;
		GeoPoint p;
		String team;
		boolean senderFound = false;
		PiViOverlayItem oldItem = null;
		PiViOverlayItem newItem = null;

		if (msg.length == 3) {
			team = msg[0];
			lat = Integer.parseInt(msg[1]);
			lng = Integer.parseInt(msg[2]);

			p = new GeoPoint(lat, lng);
			Log.w("RSM", "UPDATING LOCS");
			if (team.equals(PlayerTeam.Pirates.toString())) {
				Log.w("RSM", "PIRATE");
				for (PiViOverlayItem item : mOverlayBadPlayers.getItems()) {
					if (!senderFound && item.getSnippet().equals(sender)) {
						oldItem = item;
						senderFound = true;
					}
				}
				if (senderFound) {
					mOverlayBadPlayers.remove(oldItem);
				}

				newItem = new PiViOverlayItem(p, "", sender);
				mOverlayBadPlayers.addOverlay(newItem);
			} else if (team.equals(PlayerTeam.Vikings.toString())) {
				Log.w("RSM", "VIKING");
				for (PiViOverlayItem item : mOverlayGoodPlayers.getItems()) {
					if (!senderFound && item.getSnippet().equals(sender)) {
						oldItem = item;
						senderFound = true;
					}
				}

				if (senderFound) {
					mOverlayGoodPlayers.remove(oldItem);
				}

				newItem = new PiViOverlayItem(p, "", sender);
				mOverlayGoodPlayers.addOverlay(newItem);
			}

		}

	}

	/**
	 * Method to push GPSData to XMPP Put data in message.setMessage(String
	 * data) in format you like
	 * 
	 * @param message
	 */
	public void sendGPSCoordinates(PiviXmppMessage message) {
		Intent intent = new Intent();
		intent.setAction(XmppService.GPS_SEND_ACTION);
		intent.putExtra(XmppService.GPS_MESSAGE, message);

		Log.i(GameMapActivity.class.getSimpleName(), message.getMessage());
		if (mXmppServiceStarted) {
			sendBroadcast(intent);
		} else {
			mMessageQueue.add(intent);
		}
	}

	public void sendFeed(PiviXmppMessage message) {
		Intent intent = new Intent();
		intent.setAction(XmppService.ORB_SEND_ACTION);
		intent.putExtra(XmppService.PAR_ORB, message);

		Log.i(GameMapActivity.class.getSimpleName(), message.getMessage());
		if (mXmppServiceStarted) {
			sendBroadcast(intent);
		} else {
			mMessageQueue.add(intent);
		}
	}

	/**
	 * THIS CLASS WILL RECEIVE THE NEW GPS MESSAGES/DATA NOTE THAT WHEN YOU SEND
	 * DATA TO XMPP, YOU WILL ALSO TRIGGER THIS
	 */
	private class MyGpsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			PiviXmppMessage message = (PiviXmppMessage) intent
					.getSerializableExtra(XmppService.GPS_MESSAGE);

			GameMapActivity.this.updatePlayerLocations(message.getSender(),
					message.getMessage());

			Log.i(GameMapActivity.class.getSimpleName(), "GPSMessage: "
					+ message.getSender() + " " + message.getMessage());
		}
	}

	/**
	 * this broadcastreceiver will receive all actions, such as orbs collected
	 * etc (work in progress)
	 * 
	 */
	private class MyFeedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			FeedActions actie = (FeedActions) intent
					.getSerializableExtra(XmppService.FEED_MESSAGE);
			if (actie == FeedActions.orbCollectedBad
					|| actie == FeedActions.orbCollectedGood) {
				PiviXmppMessage message = (PiviXmppMessage) intent
						.getSerializableExtra(XmppService.ORB_COLLECTED);

				GameMapActivity.this.updateOrbLocations(message.getMessage());

				Log.i(GameMapActivity.class.getSimpleName(), "FeedMessage: "
						+ message.getMessage());
			}
		}
	}

	private class MyServiceChecker extends BroadcastReceiver {

		@Override
		public void onReceive(Context ctx, Intent intent) {
			mXmppServiceStarted = true;
			for (Intent sendIntent : mMessageQueue) {
				sendBroadcast(sendIntent);
				Log.i(GameMapActivity.class.getSimpleName(), "Sending backlog");
			}
		}

	}

	/**
	 * Var that binds the xmpp service to the lifecycle of the activity, do not
	 * touch :)
	 */
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
