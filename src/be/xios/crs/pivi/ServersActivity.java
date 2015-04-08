package be.xios.crs.pivi;

import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.Toast;
import be.xios.crs.pivi.adapters.ServerListAdapter;
import be.xios.crs.pivi.broadcastreceivers.BatteryLevelReceiver;
import be.xios.crs.pivi.managers.DbManager;
import be.xios.crs.pivi.managers.SettingsManager;
import be.xios.crs.pivi.managers.WebManager;
import be.xios.crs.pivi.models.GameServer;
import be.xios.crs.pivi.models.PlayerGameInstance;
import be.xios.crs.pivi.models.User;

public class ServersActivity extends ListActivity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	private Button btnCreate;
	private ProgressDialog progressDialog = null;
	private Context context;
	private ArrayList<GameServer> serverList = null;
	private ServersFetcher serverFetcher;
	private BatteryLevelReceiver batteryLevelReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servers);

		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);

		btnCreate = (Button) findViewById(R.id.servers_create);
		btnCreate.setOnClickListener(this);

		Button btnRefresh = (Button) findViewById(R.id.servers_refresh);
		btnRefresh.setOnClickListener(this);
		
		context = this;
		
		// BroadcastReceiver BatteryLevel
	    batteryLevelReceiver = new BatteryLevelReceiver();
	    IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_LOW");
	    registerReceiver(batteryLevelReceiver, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		serverFetcher = new ServersFetcher();
		serverFetcher.execute();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.servers_create:
			doCreate();
			break;
		case R.id.servers_refresh:
			doRefresh();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
		GameServer server = serverList.get(pos);
		User user = SettingsManager.loadAccount(getApplicationContext());
		new GameJoiner(user, server.getId()).execute();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long arg3) {
		if(pos >= 0){
			GameServer gameServer = serverList.get(pos);
			if(gameServer != null){
				buildAlertMessageDeleteServer(gameServer);
			}
		}
		return false;
	}
	
	private void buildAlertMessageDeleteServer(GameServer gameServer){
		DialogButtonClickListener dialogButtonClickListener = new DialogButtonClickListener(gameServer);
		Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.servers_dialog_delete);
		builder.setCancelable(false);
		builder.setPositiveButton(android.R.string.yes, dialogButtonClickListener);
		builder.setNegativeButton(android.R.string.no, dialogButtonClickListener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		unregisterReceiver(batteryLevelReceiver);
	}
	
	public ServersFetcher getServersFetcher(){
		return serverFetcher;
	}

	/**
	 * Method to open the create server screen
	 */
	private void doCreate() {
		Intent intent = new Intent(getApplicationContext(),
				CreateServerActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Method to refresh the server screen
	 */
	private void doRefresh(){
		serverFetcher = new ServersFetcher();
		serverFetcher.execute();
	}

	/**
	 * Asynctask to fetch the serverlist
	 * 
	 * @author Stece
	 */
	private class ServersFetcher extends
			AsyncTask<Object, Integer, ArrayList<GameServer>> {

		private ServerListAdapter serverListAdapter;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(context);
			progressDialog
					.setMessage(getString(R.string.general_fetching_servers));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected ArrayList<GameServer> doInBackground(Object... params) {
			return WebManager.getGameServers();
		}

		@Override
		protected void onPostExecute(ArrayList<GameServer> result) {
			super.onPostExecute(result);
			serverList = result;

			if (serverList != null) {
				serverListAdapter = new ServerListAdapter(context,
						R.layout.serverlist_item, serverList);

				setListAdapter(serverListAdapter);
			}
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
		}
		
		public ServerListAdapter getServerListAdapter(){
			return serverListAdapter;
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
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage(getString(R.string.general_joining_game));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected PlayerGameInstance doInBackground(Object... params) {
			return WebManager.joinServer(serverId, user);
		}

		@Override
		protected void onPostExecute(PlayerGameInstance result) {
			super.onPostExecute(result);
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}

			if (result != null) {
				DbManager dbMan = new DbManager(context);
				dbMan.deleteAllFromServer(result.getServer().getId());
				dbMan.close();
				
				Intent i = new Intent(getApplicationContext(),
						CurrentServerActivity.class);
				result.getPlayer().setUser(user);
				result.setOwner(false);
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

	private class DialogButtonClickListener implements DialogInterface.OnClickListener{

		private GameServer gameServer;
		
		public DialogButtonClickListener(GameServer gameServer) {
			this.gameServer = gameServer;
		}
		
		@Override
		public void onClick(DialogInterface arg0, int btnId) {
			if(btnId == DialogInterface.BUTTON_POSITIVE){
				ServerDeleteTask severDeleteTask = new ServerDeleteTask(gameServer);
				severDeleteTask.execute();
			}
		}
		
	}
	
	private class ServerDeleteTask extends AsyncTask<Void, Void, Boolean>{

		GameServer gameServer;
		
		public ServerDeleteTask(GameServer gameServer) {
			this.gameServer = gameServer;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage(getString(R.string.general_deleting_serer));
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			if(WebManager.deleteServer(gameServer.getId())){
				return true;
			}else{
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			
			if(result){
				ServersFetcher sf = ServersActivity.this.getServersFetcher();
				ServerListAdapter sla = sf.getServerListAdapter();
				sla.remove(gameServer);
				sla.notifyDataSetChanged();
			}
		}		
	}	
}
