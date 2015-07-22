package com.ChessLeagueManager.main;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class CustomWebViewClient extends WebViewClient {

	// Gibt es eine URL zum Laden oder ist die Anzeige selbst generiert?
	public static boolean urlToReload;
	// Wird aktuell eine URL geladen?
	public static boolean urlAtLoading;
	// Startseite oder Fehlemeldung dieser angewählt
	public static boolean urlFirst;

	public CustomWebViewClient() {
		CustomWebViewClient.urlToReload = false;
		CustomWebViewClient.urlAtLoading = false;
		CustomWebViewClient.urlFirst = true;
	}
	
	// Folge allen URLs
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		CustomWebViewClient.urlFirst = false;
		return true;
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		urlToReload = false;
		urlAtLoading = true;
		MainActivity.me.startRotating();
		// Stets versuchen zu aktiviere, Bug verursacht dabei manchmal Probleme
		MainActivity.view.getSettings().setJavaScriptEnabled(true);
		MainActivity.view.getSettings().setBuiltInZoomControls(true); // Zoomen
																		// aktiv
		MainActivity.view.getSettings().setDisplayZoomControls(false); // keine
																		// Anzeige
																		// dazu
		MainActivity.view.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);
	}
	
	public void onReceivedError(WebView view, int errorCod, String description,
			String failingUrl) {
		String summary = "<html><title>Fehler</title><body>Es ist beim Laden der Website ein Fehler aufgetreten. <br/> Bitte kontrollieren Sie die Konfiguration.</body></html>";
		urlToReload = false;
		urlAtLoading = false;
		MainActivity.me.stopRotating();
		view.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);	}

	public void onReceivedSslError(WebView view, SslErrorHandler handler,
			SslError error) {
		// Zertifikat ohne gültiges Root kann je nach Konfiguration angenommen
		// werden.
		if (MainActivity.https_ignore) {
			handler.proceed();
		} else {
			String summary = "<html><title>Fehler</title><body>Die Website besitzt kein gültiges Zertifikat. <br/> Bitte korrigieren Sie die Konfiguration oder melden Sie diesen Fehler dem Webmaster.</body></html>";
			view.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);
			urlToReload = false;
			urlAtLoading = false;
			MainActivity.me.stopRotating();
			handler.cancel();
		}
	}

	public void onPageFinished(WebView view, String url) {
		if (!url.equals("about:blank")) {
			urlToReload = true;
		} else {
			urlToReload = false;
		}
		urlAtLoading = false;
		
		if(view.getTitle()==null) {
			String summary = "<html><head><title>Fehler</title><body>Es ist beim Laden der Website ein Fehler aufgetreten. <br/> Bitte kontrollieren Sie die Konfiguration.</body></html>";
			view.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);
			urlToReload = false;
			urlAtLoading = false;
		}
		
		MainActivity.me.stopRotating();
	}

	public static void WebViewChange(boolean back) {
		if (CustomWebViewClient.urlAtLoading) {
			MainActivity.view.stopLoading();
			urlAtLoading = false;
			MainActivity.me.stopRotating();
		}
		if (!MainActivity.me.isConnection()) {
			String summary = "<html><title>Fehler</title><body>Sie besitzen keine Internetverbindung,<br/> bitte korrigieren Sie dieses Problem.</body></html>";
			MainActivity.view.loadDataWithBaseURL(null, summary, "text/html",
					"UTF-8", null);
			return;
		}
		String start;
		if (MainActivity.https) {
			start = "https://";
		} else {
			start = "http://";
		}
		if (!CustomWebViewClient.urlToReload || back) {
			MainActivity.view
					.loadUrl(start
							+ MainActivity.host
							+ "/components/com_clm/clm/index.php?view=view_report&name="
							+ MainActivity.name + "&password="
							+ MainActivity.pas);
			CustomWebViewClient.urlFirst = true;
		} else {
			MainActivity.view.reload();
		}
		return;
	}

	public static void WebViewInfo() {
		if (CustomWebViewClient.urlAtLoading) {
			MainActivity.view.stopLoading();
			urlAtLoading = false;
			MainActivity.me.stopRotating();
		}
		String versionName, changelog;
		try {
			versionName = MainActivity.me.getPackageManager().getPackageInfo(
					MainActivity.me.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = "";
		}
		AssetManager assetManager = MainActivity.me.getAssets();
		InputStream input;
		try {
			input = assetManager.open("changelog.txt");
			int size = input.available();
			byte[] buffer = new byte[size];
			input.read(buffer);
			input.close();
			changelog = new String(buffer).replaceAll("\n", "<br/>");
		} catch (IOException e) {
			changelog = "";
		}
		String summary = "<html><title>Fehler</title><body>Es ist die Version <b>" + versionName
				+ "</b> installiert.<br/><b>Changelog:</b><br/><br/>"
				+ changelog + "</body></html>";
		MainActivity.view.loadDataWithBaseURL(null, summary, "text/html",
				"UTF-8", null);
		CustomWebViewClient.urlFirst = false;
		return;
	}
}
