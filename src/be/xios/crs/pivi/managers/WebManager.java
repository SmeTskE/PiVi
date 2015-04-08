package be.xios.crs.pivi.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import be.xios.crs.pivi.enums.PlayerTeam;
import be.xios.crs.pivi.models.GameServer;
import be.xios.crs.pivi.models.Player;
import be.xios.crs.pivi.models.PlayerGameInstance;
import be.xios.crs.pivi.models.User;

public class WebManager {

	private static String WEBSERVICE_LOCATION = "http://cedriekweb.be/postgrad/";
	// METHODS
	private static String WEBSERVICE_GETSERVERS_METHOD = "getserver.php";
	private static String WEBSERVICE_INSERTSERVER_METHOD = "insertserver.php?";
	private static String WEBSERVICE_DELETESERVER_METHOD = "deleteserver.php?";
	private static String WEBSERVICE_JOINSERVER_METHOD = "insertplayer.php?";
	private static String WEBSERVICE_LEAVESERVER_METHOD = "deleteplayer.php?";
	
	/**
	 * Method to get all game servers
	 * @return List of gameservers
	 */
	public static ArrayList<GameServer> getGameServers() {
		ArrayList<GameServer> servers = null;
		
		try{
			String url = WEBSERVICE_LOCATION + WEBSERVICE_GETSERVERS_METHOD;
			InputStream stream = connectAndFetch(url);
			
			if (stream != null){
				Log.d("PIVI WEB",
						"CONNECTED AND FETCHED RESULT FROM SERVER LIST.");
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String line = null;
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				
				servers = ParseServerList(sb.toString());
			}
			
		}catch (Exception ex){
			Log.d("PIVI WEB", "COULD NOT FETCH GAME SERVERS, message: " + ex.getMessage());
			servers = null;
		}
		
		return servers;
	}

	/**
	 * Method to create a server
	 * 
	 * @param context
	 * @param mockup
	 * @return Server ID
	 */
	public static long createServer(GameServer mockup) {
		long serverId = -1;

		try {
			String urlString = WEBSERVICE_LOCATION
					+ WEBSERVICE_INSERTSERVER_METHOD + "name="
					+ mockup.getNaam() + "&time=" + mockup.getSpelduur()
					+ "&players=" + mockup.getAantalSpelers() + "&location="
					+ mockup.getLocation();
			URL url = new URL(urlString);
			InputStream stream = connectAndFetch(url.toString());

			if (stream != null) {
				Log.d("PIVI WEB",
						"CONNECTED AND FETCHED RESULT FROM CREATE SERVER.");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream));
				String result = reader.readLine();
				serverId = Long.parseLong(result);
			}
		} catch (Exception ex) {
			Log.d("PIVI WEB",
					"COULD NOT CREATE SERVER DUE TO UNKNOWN ISSUE, message: "
							+ ex.getMessage());
		}

		return serverId;
	}
	
	/**
	 * Method to delete a server
	 * @param serverId
	 * @return success/failed
	 */
	public static boolean deleteServer(long serverId){
		boolean result = false;
		
		try{
			String urlString = WEBSERVICE_LOCATION
					+ WEBSERVICE_DELETESERVER_METHOD + "id="
					+ serverId;
			URL url = new URL(urlString);
			InputStream stream = connectAndFetch(url.toString());

			if (stream != null) {
				Log.d("PIVI WEB", "CONNECTED AND FETCHED RESULT FROM DELETE SERVER.");
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String sresult = reader.readLine();
				if(Long.parseLong(sresult) == 1){
					result = true;
				}else{
					result = false;
				}
			}
		}catch(Exception ex){
			Log.d("PIVI WEB", "COULD NOT DELETE SERVER, message: " + ex.getMessage());
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Method to delete a player
	 * @param playerId
	 * @return success/failed
	 */
	public static boolean deletePlayer(long playerId){
		boolean result = false;
		
		try{
			String urlString = WEBSERVICE_LOCATION
					+ WEBSERVICE_LEAVESERVER_METHOD + "id="
					+ playerId;
			URL url = new URL(urlString);
			InputStream stream = connectAndFetch(url.toString());

			if (stream != null) {
				Log.d("PIVI WEB", "CONNECTED AND FETCHED RESULT FROM DELETE PLAYER.");
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String sresult = reader.readLine();
				if(Long.parseLong(sresult) == 1){
					result = true;
				}else{
					result = false;
				}
			}
		}catch (Exception ex){
			Log.d("PIVI WEB", "COULD NOT DELETE PLAYER, message: " + ex.getMessage());
			result = false;
		}
		
		return result;
	}

	/**
	 * Method to join a server based on serverId
	 * 
	 * @param context
	 * @param serverId
	 * @return Player information and Server information
	 */
	public static PlayerGameInstance joinServer(long serverId, User user) {
		PlayerGameInstance gameInstance = null;
		BufferedReader reader = null;
		InputStream stream = null;

		try {
			String urlString = WEBSERVICE_LOCATION
					+ WEBSERVICE_JOINSERVER_METHOD + "accountname="
					+ user.getUsername() + "&nickname=" + user.getNickname()
					+ "&serverid=" + serverId;
			URL url = new URL(urlString);
			stream = connectAndFetch(url.toString());

			if (stream != null) {
				Log.d("PIVI WEB",
						"CONNECTED AND FETCHED RESULT FROM JOIN SERVER.");
				reader = new BufferedReader(new InputStreamReader(stream));
				String line = null;
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				gameInstance = parseGameInstance(sb.toString());
			}
		} catch (Exception ex) {

			gameInstance = null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
				reader = null;
			}
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
				stream = null;
			}
		}
		return gameInstance;
	}

	/**
	 * Method to parse a jsonString to a PlayerGameInstance object
	 * @param jsonString
	 * @return PlayerGameInstance
	 */
	private static PlayerGameInstance parseGameInstance(String jsonString) {
		PlayerGameInstance instance = null;
        try {
        	JSONObject baseObject = new JSONObject(jsonString);
        	JSONObject result = (JSONObject) baseObject.get("result");
        	JSONObject server = (JSONObject) result.get("server");
        	JSONObject player = (JSONObject) result.get("player");
        	
        	instance = new PlayerGameInstance();
        	GameServer gameServer = new GameServer();
        	gameServer.setId(Long.parseLong(server.getString("id")));
        	gameServer.setNaam(server.getString("name"));
        	gameServer.setSpelduur(server.getInt("time"));
        	gameServer.setAantalSpelers(server.getInt("players"));
        	gameServer.setFeedRoom(server.getString("xmpproomfeed"));
        	gameServer.setPrivateRoomVikings(server.getString("xmpproomprivatevikings"));
        	gameServer.setPrivateRoomPirates(server.getString("xmpproomprivatepirates"));
        	gameServer.setPublicRoom(server.getString("xmpproompublic"));
        	gameServer.setGpsRoom(server.getString("xmpproomgps"));
        	instance.setServer(gameServer);
        	Player speler = new Player();        	
        	if (player.getString("team").toString().toLowerCase(Locale.getDefault()).startsWith("p")){
        		speler.setTeam(PlayerTeam.Pirates);
        	}else{
        		speler.setTeam(PlayerTeam.Vikings);
        	}
        	speler.setId(player.getInt("id"));
        	instance.setPlayer(speler);

        } catch (JSONException e) {
            Log.d("PIVI WEB", "COULD NOT PARSE INSTANCE DATA, message: " + e.getMessage());
            instance = null;
        }
		return instance;
	}

	/**
	 * Method to connect to webservice and ge the content
	 * 
	 * @param url
	 * @return InputStream with content
	 */
	private static InputStream connectAndFetch(String url) {
		InputStream result = null;

		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			result = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			Log.d("PIVI WEB",
					"COULD NOT CONNECT TO SERVER. Message: " + e.getMessage());
			result = null;
		} catch (ClientProtocolException e) {
			Log.d("PIVI WEB",
					"COULD NOT CONNECT TO SERVER. Message: " + e.getMessage());
			result = null;
		} catch (IOException e) {
			Log.d("PIVI WEB",
					"COULD NOT CONNECT TO SERVER. Message: " + e.getMessage());
			result = null;
		}

		return result;
	}

	/**
	 * Method to parse jsonString to a ArrayList of GameServers
	 * @param jsonString
	 * @return ArrayList of GameServers
	 */
	private static ArrayList<GameServer> ParseServerList(String jsonString){
		ArrayList<GameServer> serverList = null;
		
		try{
			serverList = new ArrayList<GameServer>();
			JSONObject baseObject = new JSONObject(jsonString);
			JSONArray serverArr = baseObject.getJSONArray("servers");
			
			for (int i = 0; i < serverArr.length(); i++){
				JSONObject serverJSONContainer = serverArr.getJSONObject(i);
				JSONObject serverJSON = (JSONObject) serverJSONContainer.get("server");
				GameServer gameServer = new GameServer();
				gameServer.setId(serverJSON.getLong("id"));
				gameServer.setNaam(serverJSON.getString("name"));
				gameServer.setAantalSpelers(serverJSON.getInt("players"));
				gameServer.setSpelduur(serverJSON.getInt("time"));
	        	gameServer.setFeedRoom(serverJSON.getString("xmpproomfeed"));
	        	gameServer.setPrivateRoomVikings(serverJSON.getString("xmpproomprivatevikings"));
	        	gameServer.setPrivateRoomPirates(serverJSON.getString("xmpproomprivatepirates"));
	        	gameServer.setPublicRoom(serverJSON.getString("xmpproompublic"));
	        	gameServer.setGpsRoom(serverJSON.getString("xmpproomgps"));
	        	serverList.add(gameServer);				
			}
		}catch (JSONException ex){
			Log.d("PIVI WEB", "COULD NOT PARSE INSTANCE DATA, message: " + ex.getMessage());
			serverList = null;
		}
		
		return serverList;
	}
}
