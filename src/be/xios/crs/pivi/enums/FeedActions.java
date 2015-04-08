package be.xios.crs.pivi.enums;

public enum FeedActions {

	playerJoined("[Player_Joined]"),
	gameStarted("[Game_Started]"),
	gameEnded("[Game_Ended]"),
	orbCollectedGood("[OrbCollected_good]"),
	orbCollectedBad("[OrbCollected_bad]"),
	orbSpawnedGood("[OrbSpawned_good]"),
	orbSpawnedBad("[OrbSpawned_bad]"),
	scoreUpdated("[Score_Updated]")
	;
	
	private String actionString;
	
	private FeedActions(String actionString){
		this.actionString = actionString;
	}
	
	@Override
	public String toString(){
		return actionString;
	}
}
