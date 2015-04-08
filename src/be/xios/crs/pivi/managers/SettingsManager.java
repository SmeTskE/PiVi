package be.xios.crs.pivi.managers;

import be.xios.crs.pivi.models.User;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

	private static final String PREF_KEY = "PIVI_PREFS";
	private static final String PREF_ACCOUNT_NAME = "ACCOUNT_NAME";
	private static final String PREF_ACCOUNT_PWD = "ACCOUNT_PWD";
	private static final String PREF_ACCOUNT_EMAIL = "ACCOUNT_EMAIL";
	private static final String PREF_ACCOUNT_NICK = "ACCOUNT_NICK";
	
	public static void saveAccount(Context context, User account){
		SharedPreferences prefs = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_ACCOUNT_NAME, account.getUsername());
		editor.putString(PREF_ACCOUNT_PWD, account.getPassword());
		editor.putString(PREF_ACCOUNT_EMAIL, account.getEmail());
		editor.putString(PREF_ACCOUNT_NICK, account.getNickname());
		editor.commit();	
	}
	
	public static User loadAccount(Context context){
		SharedPreferences prefs = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		User user = new User();
		user.setUsername(prefs.getString(PREF_ACCOUNT_NAME, ""));
		user.setPassword(prefs.getString(PREF_ACCOUNT_PWD, ""));
		user.setEmail(prefs.getString(PREF_ACCOUNT_EMAIL, ""));
		user.setNickname(prefs.getString(PREF_ACCOUNT_NICK, ""));
		return user;
	}
}
