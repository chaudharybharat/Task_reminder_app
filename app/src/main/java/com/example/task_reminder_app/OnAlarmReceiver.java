package com.example.task_reminder_app;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class OnAlarmReceiver extends BroadcastReceiver {

	private static final String TAG = ComponentInfo.class.getCanonicalName(); 
	
	
	@Override	
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received wake up from alarm manager.");
		
		long rowid = intent.getExtras().getLong(RemindersDbAdapter.KEY_ROWID);
		String title = intent.getExtras().getString(RemindersDbAdapter.KEY_TITLE);
		String body = intent.getExtras().getString(RemindersDbAdapter.KEY_BODY);
		String reminder_date_time = intent.getExtras().getString(RemindersDbAdapter.KEY_DATE_TIME);



		Intent notificationIntent = new Intent(context, ReminderEditActivity.class);
		notificationIntent.putExtra(RemindersDbAdapter.KEY_ROWID, rowid);

		PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification;
		PendingIntent activity;
		int id = (int)((long)rowid);

		activity = PendingIntent.getActivity(context, (int)1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationChannel channel = new NotificationChannel("alarmme_01", "AlarmMe Notifications",
					NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
		}

		notification = new Notification.Builder(context)
				.setContentIntent(activity)
				.setSmallIcon(R.drawable.icon)
				.setAutoCancel(true)
				.setContentTitle(""+title)
				.setContentText(""+body+" "+reminder_date_time)
				.setChannelId("alarmme_01")
				.build();

		notificationManager.notify((int)id, notification);


		/*WakeReminderIntentService.acquireStaticLock(context);
		Intent i = new Intent(context, ReminderService.class);
		i.putExtra(RemindersDbAdapter.KEY_ROWID, rowid);  
		i.putExtra(RemindersDbAdapter.KEY_TITLE, title);
		i.putExtra(RemindersDbAdapter.KEY_BODY, body);
		i.putExtra(RemindersDbAdapter.KEY_DATE_TIME, reminder_date_time);
		context.startService(i);*/

		 
	}
}
