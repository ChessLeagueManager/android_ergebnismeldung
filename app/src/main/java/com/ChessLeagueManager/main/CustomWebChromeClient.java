package com.ChessLeagueManager.main;

import java.io.ByteArrayOutputStream;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.TypedValue;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class CustomWebChromeClient extends WebChromeClient {

	public void onReceivedIcon (WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
        CustomWebChromeClient.saveBitmap(icon);
        CustomWebChromeClient.loadBitmap();
        CustomWebChromeClient.showBitmap();
	}
	
	public static void saveBitmap(Bitmap bit) {
		// Nur die nötige Größe Speichern
		int px = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, MainActivity.me.getResources().getDisplayMetrics()) + 0.5);
		bit = Bitmap.createScaledBitmap(bit, px, px, true);
		MainActivity.favicon = bit;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bit.compress(Bitmap.CompressFormat.PNG, 100, baos);   
		byte[] b = baos.toByteArray(); 
		SharedPreferences sharedPref = MainActivity.me.getSharedPreferences("data", 0);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("favicon", Base64.encodeToString(b, Base64.DEFAULT));
		editor.commit();
	}
	public static void loadBitmap() {
		SharedPreferences sharedPref = MainActivity.me.getSharedPreferences("data", 0);
		String encoded = sharedPref.getString("favicon", "");
		if(encoded.equals("")) {
			MainActivity.favicon = BitmapFactory.decodeResource(MainActivity.me.getResources(), R.drawable.ic_launcher);		} else {
		    byte[] b = Base64.decode(encoded, Base64.DEFAULT);
		    MainActivity.favicon = BitmapFactory.decodeByteArray(b, 0, b.length);
		}
	}
	public static void showBitmap() {
		Resources res = MainActivity.me.getResources();
		BitmapDrawable favicon = new BitmapDrawable(res,MainActivity.favicon);
		ActionBar actionBar = MainActivity.me.getActionBar();
		actionBar.setIcon(favicon);
	}
}
