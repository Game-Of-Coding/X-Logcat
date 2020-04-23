package com.gameofcoding.xlogcat.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import com.gameofcoding.xlogcat.R;
import com.gameofcoding.xlogcat.utils.AppConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import android.content.SharedPreferences;
import android.widget.CompoundButton;
import java.io.FileNotFoundException;

public class MainActivity extends Activity {
	private final String TAG = getClass().getSimpleName();

	// Patterns for detecting log line priority
	private final Pattern PATTERN_PRIORITY_VERBOSE = Pattern.compile("..-..\\s..:..:......\\s.....\\s.....\\sV\\s.*");
    private final Pattern PATTERN_PRIORITY_DEBUG = Pattern.compile("..-..\\s..:..:......\\s.....\\s.....\\sD\\s.*");
    private final Pattern PATTERN_PRIORITY_INFO = Pattern.compile("..-..\\s..:..:......\\s.....\\s.....\\sI\\s.*");
    private final Pattern PATTERN_PRIORITY_WARN = Pattern.compile("..-..\\s..:..:......\\s.....\\s.....\\sW\\s.*");
    private final Pattern PATTERN_PRIORITY_ERROR = Pattern.compile("..-..\\s..:..:......\\s.....\\s.....\\sE\\s.*");

	// Log priorities
	private final int VERBOSE = 0;
	private final int DEBUG = 1;
	private final int INFO = 2;
	private final int WARN = 3;
	private final int ERROR = 4;

  	private boolean mShouldReadLogs; // Flag for thread
	private boolean mIsWebViewReady;
    private BufferedReader mLogsReader;
	private WebView mWebView;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		mWebView = findViewById(R.id.webView);

		// Setup WebView
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebChromeClient(new WebChromeClient() {
				@Override
				public boolean onConsoleMessage(ConsoleMessage msg) {
					showToast(msg.message());
					return super.onConsoleMessage(msg);
				}
			});
		mWebView.setWebViewClient(new WebViewClient() {
				@Override public void onPageStarted(WebView view, String url, Bitmap favicon) {mIsWebViewReady = false;}
				@Override public void onPageFinished(WebView view, String url) {mIsWebViewReady = true;}
			});
		mWebView.loadUrl(AppConstants.HTML_WEB_PAGE_PATH);
	}

	@Override
    protected void onResume() {
		super.onResume();
		startReadingLogs();
    }

	@Override
	public void onBackPressed() {
		mWebView.loadUrl("javascript:scrollToBottom()");
	}
	
	@Override
    protected void onPause() {
		stopReadingLogs();
    	super.onPause();
	}

    @Override
    protected void onDestroy() {
		try {
			if (mLogsReader != null)
				mLogsReader.close();
		} catch (IOException e) {
			Log.e(TAG, "Could'nt close logs reader", e);
		}
		super.onDestroy();
    }

	/**
	 * Stores SharedPref @autoScroll
	 */
	private void setAutoScroll(boolean autoScorll) {
		getSharedPreferences(AppConstants.PreferenceConstants.PREF_NAME_LOG,
							 MODE_PRIVATE).edit().putBoolean(AppConstants.PreferenceConstants.KEY_AUTO_SCROLL, autoScorll)
			.commit();
	}

	/**
	 * Returns value of SharedPref @autoScroll
	 */
	private boolean getAutoScroll() {
		return 	getSharedPreferences(AppConstants.PreferenceConstants.PREF_NAME_LOG, MODE_PRIVATE)
			.getBoolean(AppConstants.PreferenceConstants.KEY_AUTO_SCROLL, true);
	}

	/**
	 * OnClick for floating button, settings
	 */
	public void settings(View view) {
		View dialogLayout = getLayoutInflater().inflate(R.layout.layout_options, null);
		Switch autoScroll = dialogLayout.findViewById(R.id.autoScroll);
		Button clearLog = dialogLayout.findViewById(R.id.clearLog);
		autoScroll.setChecked(getAutoScroll());
		autoScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override public void onCheckedChanged(CompoundButton button, boolean checked) {setAutoScroll(checked);}
			});
		clearLog.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (new File(getExternalCacheDir(), AppConstants.LOG_FILE_NAME).delete()) {
						mWebView.reload();
						File logFile = new File(getExternalCacheDir(), AppConstants.LOG_FILE_NAME);
						try {
							if (!logFile.exists())
								logFile.createNewFile();
							FileReader fileReader = new FileReader(logFile);
							mLogsReader = new BufferedReader(fileReader);
							showToast("Deleted!");
						} catch (Exception e) {
							showToast(e.toString());
							Log.e(TAG, "Exceptiion while deleting log file.", e);
						}
					} else showToast("Couldn't delete!");
				}
			});

		// Show dialog to user for settings
		new AlertDialog.Builder(this)
			.setTitle("Settings")
			.setView(dialogLayout)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, null)
			.create()
			.show();
	}

	/**
	 * Starts thread for reading logs
	 */
	private void startReadingLogs() {
		mShouldReadLogs = true;
		new Thread(new Runnable() {
				@Override
				public void run() {	
					try {
						if (mLogsReader == null) {
							File logFile = new File(getExternalCacheDir(), AppConstants.LOG_FILE_NAME);
							if (!logFile.exists())
								logFile.createNewFile();
							FileReader fileReader = new FileReader(logFile);
							mLogsReader = new BufferedReader(fileReader);
						}
						while (mShouldReadLogs) {
							while (!mIsWebViewReady) {
								// Wait while webview is loading
								continue;
							}
							String logLine = mLogsReader.readLine();
							if (logLine != null) {
								addLog(logLine);
								synchronized (this) {wait(40);}
							}
						}
					} catch (Exception e) {
						showToast(e.toString());
						Log.e(TAG, "Error occured while reading logs", e);
					}
				}
			}).start();
    }

	/**
	 * Stops the thread which reads logs by flag.
	 */
	private void stopReadingLogs() {
		mShouldReadLogs = false;
    }

	/**
	 * Detects the type of given log line using regex.
	 */
	private int getLogLineType(String logLine) {
		int logType = -1;
		if (PATTERN_PRIORITY_VERBOSE.matcher(logLine).matches()) logType = VERBOSE;
		else if (PATTERN_PRIORITY_DEBUG.matcher(logLine).matches()) logType = DEBUG;
		else if (PATTERN_PRIORITY_INFO.matcher(logLine).matches()) logType = INFO;
		else if (PATTERN_PRIORITY_WARN.matcher(logLine).matches()) logType = WARN;
		else if (PATTERN_PRIORITY_ERROR.matcher(logLine).matches())logType = ERROR;
		return logType;
	}
	
	/**
	 * Adds newly readed logline to webview
	 */
	private void addLog(final String logLine) {
		runOnUiThread(new Runnable() {
				@Override public void run() {
					mWebView.loadUrl("javascript:updateLogs(" + getLogLineType(logLine) + "," + "\"" + logLine + "\", " + getAutoScroll() + ")");
				}
			});
    }

	/**
	* Shows a small toast message for quick info.
	*/
	private void showToast(final String msg) {
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				}
			});
    }
}
