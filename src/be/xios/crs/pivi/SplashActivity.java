package be.xios.crs.pivi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import be.xios.crs.pivi.managers.SettingsManager;
import be.xios.crs.pivi.models.User;

public class SplashActivity extends Activity {

	// --- DECLARATIONS --- //
	private AlertDialog alert;
	boolean internet = false;
	boolean gps = false;
	private static final int REQUEST_GPS = 1;
	private static final int REQUEST_MOBILE_DATA = 0;
	private ImageView pirate;
	private ImageView viking;
	private ImageView sword;
	private ImageView axe;
	private ImageView shield;
	private ProgressBar progressbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		pirate = (ImageView) findViewById(R.id.splash_pirate);
		viking = (ImageView) findViewById(R.id.splash_viking);
		sword = (ImageView) findViewById(R.id.splash_sword);
		axe = (ImageView) findViewById(R.id.splash_axe);
		shield = (ImageView) findViewById(R.id.splash_shield);
		progressbar = (ProgressBar) findViewById(R.id.splash_progress);

		doAnimationPirate();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_MOBILE_DATA:
			internet = checkInternet();
			if (internet) {
				doStep2();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_no_Mobile_Data),
						Toast.LENGTH_LONG).show();
				finish();
			}
			break;
		case REQUEST_GPS:
			gps = checkGPS();
			if (gps) {
				startApplication();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.error_no_GPS), Toast.LENGTH_LONG)
						.show();
				finish();
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (alert != null && alert.isShowing()) {
			alert.dismiss();
			alert = null;
		}
	}

	private void doAnimationPirate() {
		Animation animationPirate = AnimationUtils.loadAnimation(this,
				R.anim.splash_pirate);
		animationPirate.setFillAfter(true);
		animationPirate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				pirate.setVisibility(View.VISIBLE);
				doAnimationViking();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				pirate.clearAnimation();
			}
		});

		pirate.clearAnimation();
		pirate.startAnimation(animationPirate);
	}

	private void doAnimationViking() {
		Animation animationViking = AnimationUtils.loadAnimation(this,
				R.anim.splash_viking);
		animationViking.setFillAfter(true);
		animationViking.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				viking.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				viking.clearAnimation();
				doAnimationSword();
			}
		});

		viking.clearAnimation();
		viking.startAnimation(animationViking);
	}

	private void doAnimationSword() {
		Animation animationSword = AnimationUtils.loadAnimation(this,
				R.anim.splash_sword);
		animationSword.setFillAfter(true);
		animationSword.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				sword.setVisibility(View.VISIBLE);
				sword.bringToFront();
				doAnimationAxe();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				sword.clearAnimation();
			}
		});

		sword.clearAnimation();
		sword.startAnimation(animationSword);
	}

	private void doAnimationAxe() {
		Animation animationAxe = AnimationUtils.loadAnimation(this,
				R.anim.splash_axe);
		animationAxe.setFillAfter(true);
		animationAxe.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				axe.setVisibility(View.VISIBLE);
				axe.bringToFront();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				axe.clearAnimation();
				doAnimationShield();
			}
		});

		axe.clearAnimation();
		axe.startAnimation(animationAxe);
	}

	private void doAnimationShield() {
		Animation animationShield = AnimationUtils.loadAnimation(this,
				R.anim.splash_shield);
		animationShield.setFillAfter(true);
		animationShield.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				shield.setVisibility(View.VISIBLE);
				shield.bringToFront();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				shield.clearAnimation();
				doAnimationProgress();
			}
		});

		shield.clearAnimation();
		shield.startAnimation(animationShield);
	}

	private void doAnimationProgress() {
		Animation animationProgress = AnimationUtils.loadAnimation(this,
				R.anim.splash_progress);
		animationProgress.setFillAfter(true);
		animationProgress.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				progressbar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				progressbar.clearAnimation();
				startConnectionChecks();
			}
		});

		progressbar.clearAnimation();
		progressbar.startAnimation(animationProgress);
	}

	private void startConnectionChecks() {
		internet = checkInternet();
		gps = checkGPS();

		doStep1();
	}

	private void doStep1() {
		if (!internet) {
			buildAlertMessageMobileData();
		} else {
			doStep2();
		}
	}

	private void doStep2() {
		if (!gps) {
			buildAlertMessageNoGps();
		} else {
			startApplication();
		}
	}

	/**
	 * Method to check if internet is available
	 * 
	 * @return true/false
	 */
	private boolean checkInternet() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		/*
		 * NetworkInfo mWifi = connManager
		 * .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		 * 
		 * if (mWifi.isConnected()) { // TODO ADVISE TO DISABLE WIFI AND USE 3G
		 * SINCE GAMER WILL TRAVEL // LARGE DISTANCES AND SHOULD SAVE BATTERY }
		 */

		NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		//return mobile != null && mobile.isAvailable() && mobile.isConnectedOrConnecting();
		return true;//srr cedriek :p
	}

	/**
	 * Method to check if GPS is enabled
	 * 
	 * @return true/flase
	 */
	private boolean checkGPS() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Method to build dialog for enableing GPS
	 */
	private void buildAlertMessageNoGps() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.GPS_dialog))
				.setCancelable(false)
				.setPositiveButton(getString(android.R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivityForResult(
										new Intent(
												android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
										REQUEST_GPS);
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
								Toast.makeText(getApplicationContext(),
										getString(R.string.error_no_GPS),
										Toast.LENGTH_LONG).show();
								finish();
							}
						});
		alert = builder.create();
		alert.show();
	}

	/**
	 * Method to build dialog for enableing 3G
	 */
	private void buildAlertMessageMobileData() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.Mobile_Data_dialog))
				.setCancelable(false)
				.setPositiveButton(getString(android.R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivityForResult(
										new Intent(
												android.provider.Settings.ACTION_SETTINGS),
										REQUEST_MOBILE_DATA);
							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
								Toast.makeText(
										getApplicationContext(),
										getString(R.string.error_no_Mobile_Data),
										Toast.LENGTH_LONG).show();
								finish();
							}
						});
		alert = builder.create();
		alert.show();
	}

	private void startApplication() {
		if (internet && gps) {
			User user = SettingsManager.loadAccount(getApplicationContext());
			if (user.getUsername().equals("")) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, ServersActivity.class);
				startActivity(intent);
			}
		}
		finish();
	}
}
