package be.xios.crs.pivi.models;

public class GameArea {

	private String name;
	private long minLongitude;
	private long maxLongitude;
	private long minLatitude;
	private long maxLatitude;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the minLongitude
	 */
	public long getMinLongitude() {
		return minLongitude;
	}
	/**
	 * @param minLongitude the minLongitude to set
	 */
	public void setMinLongitude(long minLongitude) {
		this.minLongitude = minLongitude;
	}
	/**
	 * @return the maxLongitude
	 */
	public long getMaxLongitude() {
		return maxLongitude;
	}
	/**
	 * @param maxLongitude the maxLongitude to set
	 */
	public void setMaxLongitude(long maxLongitude) {
		this.maxLongitude = maxLongitude;
	}
	/**
	 * @return the minLatitude
	 */
	public long getMinLatitude() {
		return minLatitude;
	}
	/**
	 * @param minLatitude the minLatitude to set
	 */
	public void setMinLatitude(long minLatitude) {
		this.minLatitude = minLatitude;
	}
	/**
	 * @return the maxLatitude
	 */
	public long getMaxLatitude() {
		return maxLatitude;
	}
	/**
	 * @param maxLatitude the maxLatitude to set
	 */
	public void setMaxLatitude(long maxLatitude) {
		this.maxLatitude = maxLatitude;
	}
}
