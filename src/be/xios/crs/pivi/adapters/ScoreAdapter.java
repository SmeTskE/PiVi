package be.xios.crs.pivi.adapters;

import java.util.List;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.models.Player;
import be.xios.crs.pivi.tools.ViewHolderScore;

public class ScoreAdapter extends ArrayAdapter<Player> {

	private Context mContext;
	private List<Player> mPlayers;
	private static final int ICON_PIRATE = R.drawable.icon_user_pirate;
	private static final int ICON_VIKING = R.drawable.icon_user_viking;

	public ScoreAdapter(Context context, int resource, int textViewResourceId,
			List<Player> objects) {
		super(context, resource, textViewResourceId, objects);

		mContext = context;
		mPlayers = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		ViewHolderScore vhs;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_item_score, parent, false);
			vhs = new ViewHolderScore();
			vhs.ivIcon = (ImageView) v.findViewById(R.id.iv_icon);
			vhs.tvName = (TextView) v.findViewById(R.id.tv_name);
			vhs.tvScore = (TextView) v.findViewById(R.id.tv_score);
			v.setTag(vhs);
		}

		vhs = (ViewHolderScore) v.getTag();

		Player p = mPlayers.get(position);
		Drawable icon;
		if (p.getTeam() == PlayerTeam.Pirates) {
			icon = mContext.getResources().getDrawable(ICON_PIRATE);
		} else {
			icon = mContext.getResources().getDrawable(ICON_VIKING);
		}

		vhs.ivIcon.setImageDrawable(icon);
		vhs.tvName.setText(p.getUser().getNickname());
		vhs.tvScore.setText(Integer.toString(p.getScore()));

		return v;
	}
}
