package com.example.scancolors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity implements
		ColorPickerDialog.OnColorChangedListener {

	public Context context;
	public static final String PREFS_NAME = "SettingsFile";
	private int red_colorNo1, blue_colorNo1, green_colorNo1, red_colorNo2,
			blue_colorNo2, green_colorNo2;
	private String number, message;
	EditText et_mob_number, et_mob_message;
	public SharedPreferences settings;
	public int[] rgbArr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_main);

		Button btn_scan = (Button) findViewById(R.id.btn_scan_color);
		btn_scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				et_mob_number = (EditText) findViewById(R.id.et_mob_number);
				et_mob_message = (EditText) findViewById(R.id.sms_message);
				String number = et_mob_number.getText().toString();
				String message = et_mob_message.getText().toString();
				settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("number", number);
				editor.putString("message", message);
				editor.commit();

				Intent i = new Intent(getApplicationContext(),
						ScanActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		red_colorNo1 = settings.getInt("red1", 1);
		blue_colorNo1 = settings.getInt("blue1", 1);
		green_colorNo1 = settings.getInt("green1", 1);
		red_colorNo2 = settings.getInt("red2", 255);
		blue_colorNo2 = settings.getInt("blue2", 0);
		green_colorNo2 = settings.getInt("green2", 0);
		number = settings.getString("number", "");
		message = settings.getString("message", "");
		ImageView iv_picked_color_no1 = (ImageView) findViewById(R.id.iv_picked_color_no1);
		iv_picked_color_no1.setBackgroundColor(Color.rgb(red_colorNo1,
				green_colorNo1, blue_colorNo1));
		ImageView iv_picked_color_no2 = (ImageView) findViewById(R.id.iv_picked_color_no2);
		iv_picked_color_no2.setBackgroundColor(Color.rgb(red_colorNo2,
				green_colorNo2, blue_colorNo2));
		et_mob_number = (EditText) findViewById(R.id.et_mob_number);
		et_mob_number.setText(number);
		et_mob_message = (EditText) findViewById(R.id.sms_message);
		et_mob_message.setText(message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.pic_color_1:
			Intent i = new Intent(getApplicationContext(),
					ColorPickCameraActivity.class);
			i.putExtra("color", 1);
			startActivity(i);
			return true;
		case R.id.pic_color_2:
			Intent i1 = new Intent(getApplicationContext(),
					ColorPickCameraActivity.class);
			i1.putExtra("color", 2);
			startActivity(i1);
			return true;
		case R.id.color_picker_1:
			new ColorPickerDialog(MainActivity.this, MainActivity.this, "",
					Color.BLACK, Color.WHITE, 1).show();
			return true;
		case R.id.color_picker_2:
			new ColorPickerDialog(MainActivity.this, MainActivity.this, "",
					Color.BLACK, Color.WHITE, 2).show();
			return true;
		case R.id.action_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * AsyncTask for later use, proly.
	 * 
	 */
	/*
	private class RegisterTask extends AsyncTask<Void, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(context);

		protected void onPreExecute() {
			this.dialog.setMessage("Uèitavanje...");
			this.dialog.show();
		}

		protected Boolean doInBackground(Void... params) {
			// Toast.makeText(context, "dada", Toast.LENGTH_LONG).show();
			return true;
		}

		protected void onPostExecute(final Boolean result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (result.booleanValue()) {
				// also show register success dialog
			}
		}

	}*/

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	/**
	 * Color in RGB format from Colorpicker Class
	 */
	@Override
	public void colorChanged(String str, int color, int boja) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		rgbArr = ColorPickCameraActivity.getRGBArr(color);
		if (boja == 1) {
			red_colorNo1 = rgbArr[0];
			green_colorNo1 = rgbArr[1];
			blue_colorNo1 = rgbArr[2];
			Log.w("aplikacija boja crvena:", String.valueOf(red_colorNo1));
			Log.w("aplikacija boja zelena:", String.valueOf(green_colorNo1));
			Log.w("aplikacija boja plava:", String.valueOf(blue_colorNo1));
			editor.putInt("red1", red_colorNo1);
			editor.putInt("green1", green_colorNo1);
			editor.putInt("blue1", blue_colorNo1);
			editor.commit();
		} else if (boja == 2) {
			red_colorNo2 = rgbArr[0];
			green_colorNo2 = rgbArr[1];
			blue_colorNo2 = rgbArr[2];
			Log.w("aplikacija boja crvena:", String.valueOf(red_colorNo2));
			Log.w("aplikacija boja zelena:", String.valueOf(green_colorNo2));
			Log.w("aplikacija boja plava:", String.valueOf(blue_colorNo2));
			editor.putInt("red2", red_colorNo2);
			editor.putInt("green2", green_colorNo2);
			editor.putInt("blue2", blue_colorNo2);
			editor.commit();
		}

		Intent intent = getIntent();
		finish();
		startActivity(intent);

		/* Code for no transition
		 	if (Build.VERSION.SDK_INT >= 11) {
			recreate();
		} else {
			Intent intent = getIntent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			finish();
			overridePendingTransition(0, 0);

			startActivity(intent);
			overridePendingTransition(0, 0);
		}*/
	}
}