package be.xios.crs.pivi.models;

import java.io.Serializable;

public class PlayerGameInstance implements Serializable {

	private static final long serialVersionUID = 2049638119346738681L;
	private Player player;
	private GameServer server;
	private Boolean owner;
	
	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}
	/**
	 * @param player the player to set
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
	/**
	 * @return the server
	 */
	public GameServer getServer() {
		return server;
	}
	/**
	 * @param server the server to set
	 */
	public void setServer(GameServer server) {
		this.server = server;
	}
	/**
	 * @return the owner
	 */
	public Boolean getOwner() {
		return owner;
	}
	/**
	 * @param owner the owner to set
	 */
	public void setOwner(Boolean owner) {
		this.owner = owner;
	}
	
}
