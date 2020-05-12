package com.example.task_reminder_app;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ReminderService extends WakeReminderIntentService {

	public ReminderService() {
		super("ReminderService");
			}

	@Override
	void doReminderWork(Intent intent) {
		Log.d("ReminderService", "Doing work.");
		Long rowId = intent.getExtras().getLong(RemindersDbAdapter.KEY_ROWID);
		String title = intent.getExtras().getString(RemindersDbAdapter.KEY_TITLE);
		String body = intent.getExtras().getString(RemindersDbAdapter.KEY_BODY);
		String reminder_date_time = intent.getExtras().getString(RemindersDbAdapter.KEY_DATE_TIME);

		Intent notificationIntent = new Intent(this, ReminderEditActivity.class);
		notificationIntent.putExtra(RemindersDbAdapter.KEY_ROWID, rowId);

		PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification;
		PendingIntent activity;
		int id = (int)((long)rowId);

		activity = PendingIntent.getActivity(this, (int)1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationChannel channel = new NotificationChannel("alarmme_01", "AlarmMe Notifications",
					NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
		}

		notification = new Notification.Builder(this)
				.setContentIntent(activity)
				.setSmallIcon(R.drawable.icon)
				.setAutoCancel(true)
				.setContentTitle(""+title)
				.setContentText(""+body+" "+reminder_date_time)
				.setChannelId("alarmme_01")
				.build();

		notificationManager.notify((int)id, notification);


		
	}

}
