package be.xios.crs.pivi.fragments;

import com.actionbarsherlock.app.SherlockListFragment;

import be.xios.crs.pivi.GameActivity;
import be.xios.crs.pivi.R;
import be.xios.crs.pivi.adapters.ChatListAdapter;
import be.xios.crs.pivi.enums.ChatRooms;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.models.ChatMessage;
import be.xios.crs.pivi.models.PiviXmppMessage;
import be.xios.crs.pivi.models.PlayerGameInstance;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;

public class GameChatFragment extends SherlockListFragment implements OnClickListener{
	
	public Button btnSend;
	private ChatRooms currentRoom;
	private GameActivity gameActivity;
	private ChatListAdapter globalAdapter;
	private ChatListAdapter piratesAdapter;
	private ChatListAdapter vikingsAdapter;
	private PlayerGameInstance gameInstance;
	private EditText et_message;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v;
		
		v = inflater.inflate(R.layout.fragment_chat, container, false);
		
		gameActivity = (GameActivity) getActivity();
		gameInstance = gameActivity.getGameInstance();
		btnSend = (Button) v.findViewById(R.id.btn_sendmessage);
		btnSend.setOnClickListener(this);
		et_message = (EditText) v.findViewById(R.id.ed_chat);
		currentRoom = ChatRooms.GlobalChat;
		showNewRoom();
		// btnSend.setEnabled(false);
		return v;
	}
	
	@Override
	public void onClick(View v) {
		ChatMessage chatMsg = new ChatMessage();
		PiviXmppMessage msg = new PiviXmppMessage();
		msg.setMessage(et_message.getText().toString());
		chatMsg.setXmppMessage(msg);
		if (gameInstance.getPlayer().getTeam() == PlayerTeam.Pirates){
			chatMsg.setIconId(R.drawable.icon_user_pirate);
		}else{
			chatMsg.setIconId(R.drawable.icon_user_viking);
		}
		gameActivity.SendChatMessage(chatMsg, currentRoom);
		et_message.setText("");
	}
	
	private void showNewRoom(){
		switch (currentRoom){
		case VikingsChat:
			vikingsAdapter = gameActivity.getVikingsAdapter();
			setListAdapter(vikingsAdapter);
			break;
		case PiratesChat:
			piratesAdapter = gameActivity.getPiratesAdapter();
			setListAdapter(piratesAdapter);
			break;
		case GlobalChat:
			globalAdapter = gameActivity.getGlobalAdapter();
			setListAdapter(globalAdapter);
			break;
		}
	}
}