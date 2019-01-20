package com.CLM;

import com.CLM.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

	public static Menu menu;
	public static MainActivity me;
	public static WebView view;
	public static FrameLayout webViewPlaceholder;

	// Konfigurationsparameter
	public static boolean newApp;
	public static String host;
	public static String name;
	public static String pas;
	public static boolean https;
	public static boolean https_ignore;
	public static Bitmap favicon; // Als String gespeichert

	// erster Start besitzt Sonderbehandlung
	public static boolean firstStart;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Bis API Level 19 wird diesr Aufruf für Favicons benötigt
		if(android.os.Build.VERSION.SDK_INT<19) {
			WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
		}
		
		MainActivity.me = this;
		
		// Konfigurationsparameter Auslesen
		SharedPreferences sharedPref = getSharedPreferences("data", 0);
		MainActivity.newApp = sharedPref.getBoolean("newApp", true);
		MainActivity.host = sharedPref.getString("host", "");
		MainActivity.name = sharedPref.getString("name", "");
		MainActivity.pas = sharedPref.getString("pas", "");
		MainActivity.https = sharedPref.getBoolean("https", false);
		MainActivity.https_ignore = sharedPref
				.getBoolean("https_ignore", false);
		
		if (savedInstanceState == null) {
			// WebView konfigurieren
			MainActivity.view = new WebView(MainActivity.me);
			MainActivity.view.setWebViewClient(new CustomWebViewClient());
			MainActivity.view.setWebChromeClient(new CustomWebChromeClient());
			MainActivity.firstStart = true;
		} else {
			MainActivity.view = new WebView(MainActivity.me);
			MainActivity.view.restoreState(savedInstanceState);
		}
        getActionBar().setTitle(R.string.title_activity_main);
        CustomWebChromeClient.loadBitmap();
        CustomWebChromeClient.showBitmap();
	}

	protected void onResume() {
		super.onResume();		
		MainActivity.webViewPlaceholder = ((FrameLayout) findViewById(R.id.webViewPlaceholder));
		MainActivity.webViewPlaceholder.addView(MainActivity.view);
		// wurde noch keine Konfiguration angelegt, schalte auf die zugehörige
		// Anzeige um
		if (MainActivity.newApp == true) {
			Intent myIntent = new Intent(MainActivity.me,
					SettingsActivity.class);
			startActivity(myIntent);
		}
		// Startseite falls es der erste Start ist oder die Konfiguration
		// geändert wurde
		if (SettingsActivity.change || MainActivity.firstStart) {
			CustomWebViewClient.WebViewChange(true);
			SettingsActivity.change = false;
			MainActivity.firstStart = false;
		}
	}

	public void onPause() {
		super.onPause();
		if (MainActivity.webViewPlaceholder.getChildCount()>0) {
			MainActivity.webViewPlaceholder.removeView(MainActivity.view);
		}
		return;
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		if (MainActivity.webViewPlaceholder.getChildCount()>0) {
			MainActivity.webViewPlaceholder.removeView(MainActivity.view);
		}
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_main);
		MainActivity.webViewPlaceholder = ((FrameLayout) findViewById(R.id.webViewPlaceholder));
		MainActivity.webViewPlaceholder.addView(MainActivity.view);	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		MainActivity.menu = menu;
		return super.onCreateOptionsMenu(menu);

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// Konfiguration Aufrufen
		if (id == R.id.action_settings) {
			Intent myIntent = new Intent(this, SettingsActivity.class);
			startActivity(myIntent);
			return super.onOptionsItemSelected(item);
			// Zurück (Startseite) oder Aktualisieren
		} else if (id == R.id.action_refresh || id == R.id.action_back) {
			if (id == R.id.action_refresh) {
				CustomWebViewClient.WebViewChange(false);
			} else {
				CustomWebViewClient.WebViewChange(true);
			}
			return super.onOptionsItemSelected(item);
		} else if (id == R.id.action_info) {
			CustomWebViewClient.WebViewInfo();
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	// Rotation des Refresh Button stoppen
	public void stopRotating() {
		if (MainActivity.menu != null) {
			MenuItem m = MainActivity.menu.findItem(R.id.action_refresh);
			if (m != null && m.getActionView() != null) {
				// Remove the animation.
				m.getActionView().clearAnimation();
				m.setActionView(null);
			}
		}
	}

	// Rotation des Refresh Button starten
	@SuppressLint("InflateParams")
	public void startRotating() {
		if (MainActivity.menu != null) {
			MenuItem m = MainActivity.menu.findItem(R.id.action_refresh);
			if (m != null && m.getActionView() == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ImageView iv = (ImageView) inflater.inflate(
						R.layout.iv_refresh, null);
				Animation rotation = AnimationUtils.loadAnimation(this,
						R.anim.rotate_refresh);
				rotation.setRepeatCount(Animation.INFINITE);
				iv.startAnimation(rotation);
				MenuItem refresh = MainActivity.menu
						.findItem(R.id.action_refresh);
				refresh.setActionView(iv);
			}
		}
	}


	// speichert den aktuellen Zustand des WebViews
	protected void onSaveInstanceState(Bundle outState) {
		MainActivity.view.saveState(outState);
		super.onSaveInstanceState(outState);
	}

	// besteht eine Internet Verbindung?
	public boolean isConnection() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		// Wird die Startseite oder einer ihrere Fehlermeldungen angezeigt, so
		// gibt es hier nichts mehr zu tun
		if (CustomWebViewClient.urlFirst) {
			if (MainActivity.webViewPlaceholder.getChildCount()>0) {
				MainActivity.webViewPlaceholder.removeView(MainActivity.view);
			}
			super.onBackPressed();
		} else {
			CustomWebViewClient.WebViewChange(true);
		}
	}
}