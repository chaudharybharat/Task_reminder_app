package com.example.task_reminder_app;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderManager {
	private static final long INTERVAL_EVERYDAY = 24 * 60 * 60 * 1000;
	private static final long INTERVAL_WEEKLY = 7 * 24 * 60 * 60 * 1000;
	private Context mContext; 
	private AlarmManager mAlarmManager;
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";

	public ReminderManager(Context context) {
		mContext = context; 
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	
	public void setReminder(Long taskId,String title,String messge,String date_time, Calendar when) {

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);

		String reminderDateTime = dateTimeFormat.format(when.getTimeInMillis());

		Log.e("test"," set alarm time ==========>>>"+reminderDateTime);
        Intent intent = new Intent(mContext, OnAlarmReceiver.class);
		intent.putExtra(RemindersDbAdapter.KEY_ROWID, (long)taskId);
		intent.putExtra(RemindersDbAdapter.KEY_TITLE,title);
		intent.putExtra(RemindersDbAdapter.KEY_BODY,messge);
		intent.putExtra(RemindersDbAdapter.KEY_DATE_TIME,date_time);


        PendingIntent pi = PendingIntent.getBroadcast(mContext, taskId.intValue(), intent,  PendingIntent.FLAG_UPDATE_CURRENT);

       // mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(),INTERVAL_WEEKLY, pi);
	}
}
