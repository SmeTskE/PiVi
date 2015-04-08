package be.xios.crs.pivi.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PiViOverlayItem extends OverlayItem {

	public PiViOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
	}

}
