package be.xios.crs.pivi.fragments;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListFragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.adapters.ScoreAdapter;
import be.xios.crs.pivi.models.Player;

public class GameScoreFragment extends SherlockListFragment {

	private List<Player> players;
	private ScoreAdapter aaScore;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v;

		v = inflater.inflate(R.layout.fragment_scoreboard, container, false);

		players = new ArrayList<Player>();
		aaScore = new ScoreAdapter(getActivity(), R.layout.simple_spinner_item,
				R.id.tv_name, players);
		setListAdapter(aaScore);

		return v;
	}

	public void setPlayerData(List<Player> players) {
		this.players.clear();
		this.players = players;
		aaScore.notifyDataSetChanged();
	}
}