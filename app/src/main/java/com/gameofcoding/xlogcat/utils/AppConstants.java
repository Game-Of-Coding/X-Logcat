package com.gameofcoding.xlogcat.utils;

public abstract class AppConstants {
	public static final String HTML_WEB_PAGE_PATH = "file:///android_asset/index.html";
	public static final String LOG_FILE_NAME = "app_logs.txt";
	public static final String KEY_APP_LOG_LINE = "app_log_cat_line";
	public static final String KEY_APP_PACKAGE_NAME = "app_package_name";
	public static abstract class PreferenceConstants {
		public static final String PREF_NAME_LOG = "log_prefs";
		public static String KEY_AUTO_SCROLL = "auto_scroll";
	}
}
