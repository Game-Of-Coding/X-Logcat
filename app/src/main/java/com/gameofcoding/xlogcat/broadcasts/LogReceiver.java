package com.gameofcoding.xlogcat.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.gameofcoding.xlogcat.R;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.gameofcoding.xlogcat.utils.AppConstants;

public class LogReceiver extends BroadcastReceiver {
	String TAG = "LogReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(context.getString(R.string.action_app_log))) {
			File logFile = new File(context.getExternalCacheDir(), AppConstants.LOG_FILE_NAME);
			try {
				FileWriter fwLogWriter = new FileWriter(logFile, true);
				BufferedWriter brLogWriter = new BufferedWriter(fwLogWriter);
				brLogWriter.write(intent.getStringExtra(AppConstants.KEY_APP_LOG_LINE) + "\n");
				brLogWriter.close();
				fwLogWriter.close();
			} catch (IOException e) {
				Log.e(TAG, "LogManager(Context): Could not load file.", e);
			}
		}
	}

}
