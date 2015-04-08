package be.xios.crs.pivi.models;

import java.io.Serializable;

public class GameServer implements Serializable {

	private static final long serialVersionUID = 5807812586518785507L;
	private long id;
	private String naam;
	private int spelduur;
	private int aantalSpelers;
	private String FeedRoom;
	private String privateRoomVikings;
	private String privateRoomPirates;
	private String publicRoom;
	private String gpsRoom;
	private String location;
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the naam
	 */
	public String getNaam() {
		return naam;
	}
	/**
	 * @param naam the naam to set
	 */
	public void setNaam(String naam) {
		this.naam = naam;
	}
	/**
	 * @return the spelduur
	 */
	public int getSpelduur() {
		return spelduur;
	}
	/**
	 * @param spelduur the spelduur to set
	 */
	public void setSpelduur(int spelduur) {
		this.spelduur = spelduur;
	}
	/**
	 * @return the aantalSpelers
	 */
	public int getAantalSpelers() {
		return aantalSpelers;
	}
	/**
	 * @param aantalSpelers the aantalSpelers to set
	 */
	public void setAantalSpelers(int aantalSpelers) {
		this.aantalSpelers = aantalSpelers;
	}
	/**
	 * @return the feedRoom
	 */
	public String getFeedRoom() {
		return FeedRoom;
	}
	/**
	 * @param feedRoom the feedRoom to set
	 */
	public void setFeedRoom(String feedRoom) {
		FeedRoom = feedRoom;
	}
	/**
	 * @return the privateRoomVikings
	 */
	public String getPrivateRoomVikings() {
		return privateRoomVikings;
	}
	/**
	 * @param privateRoomVikings the privateRoomVikings to set
	 */
	public void setPrivateRoomVikings(String privateRoomVikings) {
		this.privateRoomVikings = privateRoomVikings;
	}
	/**
	 * @return the privateRoomPirates
	 */
	public String getPrivateRoomPirates() {
		return privateRoomPirates;
	}
	/**
	 * @param privateRoomPirates the privateRoomPirates to set
	 */
	public void setPrivateRoomPirates(String privateRoomPirates) {
		this.privateRoomPirates = privateRoomPirates;
	}
	/**
	 * @return the publicRoom
	 */
	public String getPublicRoom() {
		return publicRoom;
	}
	/**
	 * @param publicRoom the publicRoom to set
	 */
	public void setPublicRoom(String publicRoom) {
		this.publicRoom = publicRoom;
	}
	/**
	 * @return the gpsRoom
	 */
	public String getGpsRoom() {
		return gpsRoom;
	}
	/**
	 * @param gpsRoom the gpsRoom to set
	 */
	public void setGpsRoom(String gpsRoom) {
		this.gpsRoom = gpsRoom;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
