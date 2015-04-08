package be.xios.crs.pivi.adapters;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.models.Player;
import be.xios.crs.pivi.tools.ViewHolderPlayer;

public class PlayerListAdapter extends ArrayAdapter<Player>{

	private ArrayList<Player> players;
	
	public PlayerListAdapter(Context context, int textViewResourceId,
			ArrayList<Player> players) {
		super(context, textViewResourceId, players);
		this.players = players;
	}
	
	@Override
	public int getCount() {
		return  players.size();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderPlayer holder;
		
		if (convertView == null){
			LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.player_list_item, parent, false);
			
			holder = new ViewHolderPlayer();
			holder.iv_icon = (ImageView) convertView.findViewById(R.id.playerlist_icon);
			holder.tv_naam = (TextView) convertView.findViewById(R.id.playerlist_name);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolderPlayer) convertView.getTag();
		}
		
		Player player = players.get(position);
		if (player != null){
			holder.tv_naam.setText(player.getUser().getNickname());

			if (player.getTeam() == PlayerTeam.Pirates){
				holder.iv_icon.setImageResource(R.drawable.icon_user_pirate);
			}else{
				holder.iv_icon.setImageResource(R.drawable.icon_user_viking);
			}
		}
		return convertView;
	}
}
