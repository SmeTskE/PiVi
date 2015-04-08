package be.xios.crs.pivi.adapters;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.models.PiviXmppMessage;
import be.xios.crs.pivi.tools.ViewHolderFeed;

public class FeedAdapter extends ArrayAdapter<PiviXmppMessage> {

	private Context mContext;
	private List<PiviXmppMessage> mFeed;

	public FeedAdapter(Context context, int resource, int textViewResourceId,
			List<PiviXmppMessage> objects) {
		super(context, resource, textViewResourceId, objects);

		mContext = context;
		mFeed = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolderFeed vhf;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_item_feed, parent, false);
			vhf = new ViewHolderFeed();
			vhf.text1 = (TextView) v.findViewById(R.id.tv_message);
			v.setTag(vhf);
		}

		vhf = (ViewHolderFeed) v.getTag();

		PiviXmppMessage msg = mFeed.get(position);
		vhf.text1.setText(msg.getMessage() + " " + msg.getSender());

		return v;
	}
}
