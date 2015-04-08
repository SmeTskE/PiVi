package be.xios.crs.pivi.models;

import java.io.Serializable;

import be.xios.crs.pivi.enums.PlayerTeam;

public class Player implements Serializable {

	private static final long serialVersionUID = 7735043572654603328L;
	
	private int id;
	private User user;
	private int mScore;
	private PlayerTeam mTeam;
	
	/*public Player(String name, int team) {
		mName = name;
		mTeam = team;
		mScore = 0;		
	}*/
	
	/*public Player(String name, int team, int score) {
		mName = name;
		mTeam = team;
		mScore = score;
	}
*/
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the mScore
	 */
	public int getScore() {
		return mScore;
	}

	/**
	 * @param mScore the mScore to set
	 */
	public void setScore(int mScore) {
		this.mScore = mScore;
	}

	/**
	 * @return the mTeam
	 */
	public PlayerTeam getTeam() {
		return mTeam;
	}

	/**
	 * @param mTeam the mTeam to set
	 */
	public void setTeam(PlayerTeam mTeam) {
		this.mTeam = mTeam;
	}
}
