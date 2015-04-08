package be.xios.crs.pivi;

import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;

import be.xios.crs.pivi.managers.SettingsManager;
import be.xios.crs.pivi.managers.WebManager;
import be.xios.crs.pivi.managers.XmppManager;
import be.xios.crs.pivi.models.GameServer;
import be.xios.crs.pivi.models.PlayerGameInstance;
import be.xios.crs.pivi.models.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class CreateServerActivity extends Activity implements OnClickListener {

	private Button btnCreateServer;
	private EditText etServerName;
	private Spinner spPlaytime;
	private Spinner spPlayers;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_server);

		btnCreateServer = (Button) findViewById(R.id.createserver_create);
		btnCreateServer.setOnClickListener(this);

		etServerName = (EditText) findViewById(R.id.createserver_servername);
		spPlaytime = (Spinner) findViewById(R.id.createserver_playtime);
		spPlayers = (Spinner) findViewById(R.id.createserver_players);
		SmackAndroid.init(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.createserver_create:
			doCreateGameServer();
			break;
		}
	}

	/**
	 * Method to create the gameserver based on input.
	 */
	private void doCreateGameServer() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.general_creating_game));
		progressDialog.setCancelable(false);
		progressDialog.show();

		GameServer server = new GameServer();
		server.setAantalSpelers(Integer.parseInt(spPlayers.getSelectedItem()
				.toString()));
		server.setSpelduur(Integer.parseInt(spPlaytime.getSelectedItem()
				.toString().split(" ")[0]));
		server.setNaam(etServerName.getText().toString());
		server.setLocation("Hasselt"); // TODO get from real location?

		new GameCreator(server).execute();

	}

	/**
	 * Method to join a server
	 * 
	 * @param serverId
	 */
	private void doJoinGame(long serverId) {
		if (progressDialog != null) {
			progressDialog.setMessage(getString(R.string.general_joining_game));
		}
		User user = SettingsManager.loadAccount(getApplicationContext());
		new GameJoiner(user, serverId).execute();
	}

	private class GameCreator extends AsyncTask<Object, Integer, Long> {

		private GameServer mockupServer;

		public GameCreator(GameServer server) {
			mockupServer = server;
		}

		@Override
		protected Long doInBackground(Object... params) {
			return WebManager.createServer(mockupServer);
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			if (result > 0) {
				doJoinGame(result);
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_create_server),
						Toast.LENGTH_LONG).show();
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		}
	}

	private class GameJoiner extends
			AsyncTask<Object, Integer, PlayerGameInstance> {

		private User user;
		private long serverId;

		public GameJoiner(User user, long serverId) {
			this.user = user;
			this.serverId = serverId;
		}

		@Override
		protected PlayerGameInstance doInBackground(Object... params) {
			PlayerGameInstance result = WebManager.joinServer(serverId, user);
			
			XMPPConnection conn = XmppManager.connect();
			XmppManager.logIn(conn, user);
			XmppManager.createChatGroup(conn, user, result.getServer().getFeedRoom());
			XmppManager.createChatGroup(conn, user, result.getServer().getGpsRoom());
			XmppManager.createChatGroup(conn, user, result.getServer().getPrivateRoomPirates());
			XmppManager.createChatGroup(conn, user, result.getServer().getPrivateRoomVikings());
			XmppManager.createChatGroup(conn, user, result.getServer().getPublicRoom());
			
			XmppManager.disconnect(conn);
			
			return result;
		}

		@Override
		protected void onPostExecute(PlayerGameInstance result) {
			super.onPostExecute(result);
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}

			if (result != null) {
				Intent i = new Intent(getApplicationContext(),
						CurrentServerActivity.class);
				result.getPlayer().setUser(
						SettingsManager.loadAccount(getApplicationContext()));
				result.setOwner(true);
				i.putExtra(CurrentServerActivity.INTENT_SERVER_INFORMATION,
						result);
				startActivity(i);
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_join_server),
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
