package be.xios.crs.pivi.maps;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class PiViOverlay extends ItemizedOverlay<PiViOverlayItem> {

	private ArrayList<PiViOverlayItem> mOverlays = new ArrayList<PiViOverlayItem>();
	
	public PiViOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	@Override
	protected PiViOverlayItem createItem(int index) {
		return mOverlays.get(index);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}
	
	public void addOverlay(PiViOverlayItem item) {
		mOverlays.add(item);
		populate();
	}
	
	public boolean remove(PiViOverlayItem item) {
		boolean removed = mOverlays.remove(item);
		populate();
		return removed;
	}
	
	public List<PiViOverlayItem> getItems() {
		return mOverlays;
	}
	
	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		// return super.onTap(arg0, arg1);
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0, MapView arg1) {
		// return super.onTouchEvent(arg0, arg1);
		return false;
	}

}
