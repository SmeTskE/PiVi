package be.xios.crs.pivi;

import be.xios.crs.pivi.managers.SettingsManager;
import be.xios.crs.pivi.managers.XmppManager;
import be.xios.crs.pivi.models.User;
import be.xios.crs.pivi.tools.PasswordGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private Spinner accountSpinner;
	private EditText etNickname;
	private Button loginButton;
	private ProgressDialog progressDialog;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Window w = getWindow();
		w.setFormat(PixelFormat.RGBA_8888);

		accountSpinner = (Spinner) findViewById(R.id.login_account_spinner);
		loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setOnClickListener(this);
		etNickname = (EditText) findViewById(R.id.login_nickname_et);

		AccountManager am = AccountManager.get(getApplicationContext());
		Account[] accs = am.getAccountsByType("com.google");
		String[] accountNames = new String[accs.length];
		for (int i = 0; i < accs.length; i++) {
			accountNames[i] = accs[i].name;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.simple_spinner_item,
				accountNames);
		accountSpinner.setAdapter(adapter);

		context = this;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_button:
			doLogin();
			break;
		}
	}

	/**
	 * Method to save user account settings
	 */
	private void doLogin() {
		new Inlogger().execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	private class Inlogger extends AsyncTask<Object, Integer, Boolean> {

		String accountName;
		String nickName;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage(getString(R.string.general_logging));
			progressDialog.setCancelable(false);
			progressDialog.show();

			accountName = accountSpinner.getSelectedItem().toString();
			nickName = etNickname.getText().toString();
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			User user = new User();

			user.setEmail(accountName);
			user.setUsername(accountName.split("@")[0]);
			user.setPassword(PasswordGenerator.generatePassword());
			user.setNickname(nickName);
			SettingsManager.saveAccount(getApplicationContext(), user);

			return XmppManager.createAccount(XmppManager.connect(), user);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}

			if (result) {
				Intent intent = new Intent(getApplicationContext(),
						ServersActivity.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_createaccount),
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
