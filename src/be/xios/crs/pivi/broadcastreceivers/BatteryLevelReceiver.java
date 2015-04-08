package be.xios.crs.pivi.broadcastreceivers;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class BatteryLevelReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DialogButtonClickListener dialogButtonClickListener = new DialogButtonClickListener();
		Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Battery Low :(");
		builder.setCancelable(true);
		builder.setNeutralButton(android.R.string.ok, dialogButtonClickListener);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private class DialogButtonClickListener implements DialogInterface.OnClickListener{
		
		@Override
		public void onClick(DialogInterface arg0, int btnId) {
			
		}
		
	}
	
}
