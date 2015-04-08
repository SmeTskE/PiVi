package be.xios.crs.pivi.listeners;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import be.xios.crs.pivi.GameMapActivity;
import be.xios.crs.pivi.XmppService;
import be.xios.crs.pivi.enums.FeedActions;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.managers.XmppManager;
import be.xios.crs.pivi.maps.PiViOverlay;
import be.xios.crs.pivi.maps.PiViOverlayItem;
import be.xios.crs.pivi.models.PiviXmppMessage;
import be.xios.crs.pivi.models.PlayerGameInstance;

import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class PiViLocationOverlay extends MyLocationOverlay {

	private MapView mMapView;
	private Context mContext;
	private PlayerGameInstance mPlayer;
	private PiViOverlay mGoodOrbs;
	private PiViOverlay mBadOrbs;
	
	private static final int ORB_BAD = 1;
	private static final int ORB_GOOD = 2;
	
	public PiViLocationOverlay(Context ctx, MapView mv, PlayerGameInstance player) {
		super(ctx, mv);
		mContext = ctx;
		mMapView = mv;
		mPlayer = player;
	}
	
	public void setOverlayGoodOrbs(PiViOverlay overlay) {
		mGoodOrbs = overlay;
	}
	public void setOverlayBadOrbs(PiViOverlay overlay) {
		mBadOrbs = overlay;
	}
	
	
	@Override
	public synchronized void onLocationChanged(Location loc) {
		super.onLocationChanged(loc);
		Log.w(PiViLocationOverlay.class.getSimpleName(), "GOT LOC");
		
		// Update user location
		int lat = (int) (loc.getLatitude() * 1E6);
		int lng = (int) (loc.getLongitude() * 1E6);
		PiviXmppMessage msg = new PiviXmppMessage();
		msg.setMessage(mPlayer.getPlayer().getTeam() + ";;" + Integer.toString(lat) + ";;" + Integer.toString(lng));
		if (mContext instanceof GameMapActivity) {
			((GameMapActivity) mContext).sendGPSCoordinates(msg);
		}
		
		if (mGoodOrbs != null || mBadOrbs != null) {
			// TODO fix: player.getPlayer().getTeam();
			if (mPlayer.getPlayer().getTeam() == PlayerTeam.Pirates) {
				this.checkDistance(loc, mBadOrbs, ORB_BAD);
			} else {
				this.checkDistance(loc, mGoodOrbs, ORB_GOOD);
			}
		}
		
	}
	
	private synchronized void checkDistance(Location loc, PiViOverlay orbsOverlay, int orbType) {
		Location orbLoc;
		
		List<PiViOverlayItem> pickedOrbs = new ArrayList<PiViOverlayItem>(); 
		
		for (PiViOverlayItem item : orbsOverlay.getItems()) {
			orbLoc = new Location(loc);
			orbLoc.setLatitude(item.getPoint().getLatitudeE6() / 1E6);
			orbLoc.setLongitude(item.getPoint().getLongitudeE6() / 1E6);
			if (loc.distanceTo(orbLoc) < 25) {
				pickedOrbs.add(item);
			}
		}
		for (PiViOverlayItem item : pickedOrbs) {
			// orbsOverlay.remove(item);

			int lat = item.getPoint().getLatitudeE6();
			int lng = item.getPoint().getLongitudeE6();
			PiviXmppMessage msg = new PiviXmppMessage();
			String orbTypeStr = "";
			
			if (orbType == ORB_BAD) {
				orbTypeStr = FeedActions.orbCollectedBad.toString();
			} else {
				orbTypeStr = FeedActions.orbCollectedGood.toString();
			}
			msg.setMessage(orbTypeStr + ";;" + lat + ";;" + lng);				

			
			Intent intent = new Intent();
			intent.setAction(XmppService.ORB_SEND_ACTION);
			intent.putExtra(XmppService.PAR_ORB, msg);
			mContext.sendBroadcast(intent);
		}
	}

}
