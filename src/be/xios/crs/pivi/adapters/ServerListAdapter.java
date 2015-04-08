package be.xios.crs.pivi.adapters;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.models.GameServer;
import be.xios.crs.pivi.tools.ViewHolderServer;

public class ServerListAdapter extends ArrayAdapter<GameServer> {

	private ArrayList<GameServer> gameServers;

	public ServerListAdapter(Context context, int textViewResourceId,
			ArrayList<GameServer> servers) {
		super(context, textViewResourceId, servers);
		this.gameServers = servers;
	}

	@Override
	public int getCount() {
		return gameServers.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderServer holder;

		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.serverlist_item, parent, false);

			holder = new ViewHolderServer();
			holder.tv_info = (TextView) convertView
					.findViewById(R.id.serverlist_item_info);
			holder.tv_naam = (TextView) convertView
					.findViewById(R.id.serverlist_item_naam);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderServer) convertView.getTag();
		}

		GameServer server = gameServers.get(position);
		if (server != null) {
			holder.tv_naam.setText(server.getNaam());
			String info = "Spelers: " + server.getAantalSpelers()
					+ " | Speelduur: " + server.getSpelduur();
			holder.tv_info.setText(info);
		}
		return convertView;
	}
}
