package be.xios.crs.pivi.models;

import com.google.android.maps.GeoPoint;

import be.xios.crs.pivi.enums.OrbType;

public class Orb {

	private int longitude;
	private int latitude;
	private OrbType orbType;
	private GeoPoint point;
	
	public Orb() {
		this.latitude = 0;
		this.longitude = 0;
	}
	
	public long getLongitude() {
		return longitude;
	}
	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}
	public long getLatitude() {
		return latitude;
	}
	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}
	public OrbType getOrbType() {
		return orbType;
	}
	public void setOrbType(OrbType orbType) {
		this.orbType = orbType;
	}
	public GeoPoint getPoint() {
		if (this.point == null) {
			this.point = new GeoPoint(this.latitude, this.longitude);
		}
		return this.point;
	}
}
