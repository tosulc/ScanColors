package com.example.scancolors;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SecondActivity extends Activity {

	private static Context context;
	public static final String PREFS_NAME = "SettingsFile";
	public static BroadcastReceiver r_sent, r_received;
	private static int one_instance;
	private String number, message;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		context = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second_activity);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		number = settings.getString("number", "");
		message = settings.getString("message", "");
		one_instance = getIntent().getExtras().getInt("only_one_instance");
		if (number != "" && message != "" && one_instance == 1) {
			sendSMS(number, message);
			one_instance++;
		} else if (one_instance != 1) {
			Toast.makeText(context, "Only one SMS is being sent!", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(context, "Choose a number first!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * Method to send sms. Needs check, maybe even some BroadcasteReciever to
	 * get the actions of sms being sent or being delivered.
	 * 
	 * @param phoneNumber
	 * @param message
	 */
	public static void sendSMS(final String phoneNumber, String message) {
		SmsManager smsManager = SmsManager.getDefault();

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
				new Intent(DELIVERED), 0);

		smsManager.sendTextMessage(phoneNumber, null, message, sentPI,
				deliveredPI);
		Toast.makeText(context, "SMS sent!", Toast.LENGTH_SHORT).show();
	}

}
