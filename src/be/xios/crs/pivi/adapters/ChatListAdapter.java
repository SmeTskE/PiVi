package be.xios.crs.pivi.adapters;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.models.ChatMessage;
import be.xios.crs.pivi.tools.ViewHolderChat;

public class ChatListAdapter extends ArrayAdapter<ChatMessage> {

	private List<ChatMessage> chatMessages;

	public ChatListAdapter(Context context, int textViewResourceId,
			List<ChatMessage> chats) {
		super(context, textViewResourceId, chats);
		this.chatMessages = chats;
	}

	@Override
	public int getCount() {
		return chatMessages.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderChat holder;

		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.list_item_chat, parent, false);

			holder = new ViewHolderChat();
			holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
			holder.tv_datetime = (TextView) convertView
					.findViewById(R.id.tv_datetime);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_message = (TextView) convertView
					.findViewById(R.id.tv_message);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderChat) convertView.getTag();
		}

		ChatMessage msg = chatMessages.get(position);
		if (msg != null) {
			Date dte = msg.getXmppMessage().getMessageSend();
			String timeString = DateFormat.getTimeInstance().format(dte);
			holder.tv_datetime.setText(timeString);
			holder.tv_message.setText(msg.getXmppMessage().getMessage()
					.toString());
			holder.tv_name.setText(msg.getXmppMessage().getSender().toString().split("/")[1]);
		}
		return convertView;
	}
}
