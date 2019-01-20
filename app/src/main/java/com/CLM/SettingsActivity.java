package com.CLM;

import com.CLM.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	private SettingsActivity me;

	// GUI Elemente zur Konfiguration
	private EditText host;
	private EditText name;
	private EditText pas;
	private CheckBox https;
	private CheckBox https_ignore;

	public static boolean change;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		me = this;

		// GUI Elemente finden
		host = (EditText) findViewById(R.id.editText1);
		name = (EditText) findViewById(R.id.editText2);
		pas = (EditText) findViewById(R.id.editText3);
		https = (CheckBox) findViewById(R.id.checkBox1);
		https_ignore = (CheckBox) findViewById(R.id.checkBox2);
		final Button button2 = (Button) findViewById(R.id.button2);
		final Button button1 = (Button) findViewById(R.id.button1);

		// Aktuelle Konfiguration Speichern
		host.setText(MainActivity.host);
		name.setText(MainActivity.name);
		pas.setText(MainActivity.pas);
		https.setChecked(MainActivity.https);
		https_ignore.setChecked(MainActivity.https_ignore);

		// Zurück ist ohne Konfiguration nicht erlaubt
		if (MainActivity.newApp) {
			button2.setEnabled(false);
		}

		// Speichern
		button1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Eingaben auslesen und temporär Speichern
				MainActivity.host = host.getText().toString();
				MainActivity.name = name.getText().toString();
				MainActivity.pas = pas.getText().toString();
				MainActivity.https = https.isChecked();
				MainActivity.https_ignore = https_ignore.isChecked();
				MainActivity.newApp = false;
				// und nun auch permanent Speichern
				SharedPreferences sharedPref = getSharedPreferences("data", 0);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("host", MainActivity.host);
				editor.putString("name", MainActivity.name);
				editor.putString("pas", MainActivity.pas);
				editor.putBoolean("https", MainActivity.https);
				editor.putBoolean("https_ignore", MainActivity.https_ignore);
				editor.putBoolean("newApp", false); // nun nicht mehr neu
				editor.remove("favicon"); // zurücksetzen des favicon
				editor.commit();
		        CustomWebChromeClient.loadBitmap();
		        CustomWebChromeClient.showBitmap();
				SettingsActivity.change = true; // Reload wird ausgelöst
				me.finish();
			}
		});
		// Zurück
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SettingsActivity.change = false; // Reload wird NICHT ausgelöst
				me.finish();
			}
		});

	}
}
