package be.xios.crs.pivi;

import java.util.ArrayList;
import java.util.List;

import be.xios.crs.pivi.adapters.ScoreAdapter;
import be.xios.crs.pivi.models.Player;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GameScoreFragment extends ListFragment{
	
	private List<Player> players;
	private ScoreAdapter sa;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v;
		
		v = inflater.inflate(R.layout.fragment_scoreboard, container, false);
		
		// INIT TEST CODE
		players = new ArrayList<Player>();
		/*Player p1 = new Player("Cedriek", Player.TEAM_VIKING, 75);
		Player p2 = new Player("Ruben", Player.TEAM_PIRATE, 50);
		Player p3 = new Player("Stephan", Player.TEAM_VIKING, 25);
		players.add(p1);
		players.add(p2);
		players.add(p3);*/
		
	    sa = new ScoreAdapter(getActivity(), R.layout.simple_spinner_item,
				R.id.tv_name, players);
		
		setListAdapter(sa);
		
		return v;
	}
	
	
	public void setPlayerData(List<Player> players) {
		this.players.clear();
		this.players = players;
		sa.notifyDataSetChanged();
	}

}
