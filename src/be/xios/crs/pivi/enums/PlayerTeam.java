package be.xios.crs.pivi.enums;

public enum PlayerTeam {

	Pirates("Pirates"),
	Vikings("Vikings");
	
	private String teamString;
	
	private PlayerTeam(String teamString){
		this.teamString = teamString;
	}
	
	@Override
	public String toString(){
		return teamString;
	}
	
}
