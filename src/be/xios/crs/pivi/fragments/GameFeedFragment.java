package be.xios.crs.pivi.fragments;

import com.actionbarsherlock.app.SherlockListFragment;

import be.xios.crs.pivi.GameActivity;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.adapters.FeedAdapter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GameFeedFragment extends SherlockListFragment {
		
	GameActivity gameActivity;
	private FeedAdapter feedAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v;
		
		v = inflater.inflate(R.layout.fragment_feed, container, false);
		
		gameActivity = (GameActivity) getActivity();

		feedAdapter = gameActivity.getFeedAdapter();		
		setListAdapter(feedAdapter);
		
		return v;
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
}