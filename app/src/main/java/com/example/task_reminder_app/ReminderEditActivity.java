
package com.example.task_reminder_app;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


public class ReminderEditActivity extends Activity implements AdapterView.OnItemSelectedListener {

	// 
	// Dialog Constants
	//
	private static final int DATE_PICKER_DIALOG = 0;
	private static final int TIME_PICKER_DIALOG = 1;
	
	// 
	// Date Format 
	//
	private static final String DATE_FORMAT = "yyyy-MM-dd"; 
	private static final String TIME_FORMAT = "kk:mm";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
	
	private EditText mTitleText;
    private EditText mBodyText;
    private Button mDateButton;
    private Button mTimeButton;
    private Button mConfirmButton;
    private Long mRowId;
    private RemindersDbAdapter mDbHelper;
    private Calendar mCalendar;
    int HOUR=24;
    int MIN=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new RemindersDbAdapter(this);
        
        setContentView(R.layout.reminder_edit);
         initWeeklySpinner();
        mCalendar = Calendar.getInstance(); 
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mDateButton = (Button) findViewById(R.id.reminder_date);
        mTimeButton = (Button) findViewById(R.id.reminder_time);
      
        mConfirmButton = (Button) findViewById(R.id.confirm);
       
        mRowId = savedInstanceState != null ? savedInstanceState.getLong(RemindersDbAdapter.KEY_ROWID) : null;

		HOUR = new Time(System.currentTimeMillis()).getHours();
		MIN = new Time(System.currentTimeMillis()).getMinutes();
		Log.e("test","========="+HOUR+"======"+MIN);
        registerButtonListenersAndSetDefaultText();

		calanderTimeNextDay();
    }



	private void initWeeklySpinner() {
		Spinner spinner = (Spinner) findViewById(R.id.spinner);

		// Spinner click listener
		spinner.setOnItemSelectedListener(this);

		// Spinner Drop down elements
		List<String> categories = new ArrayList<String>();
		categories.add("Monday");
		categories.add("Tuesday");
		categories.add("Wednesday");
		categories.add("Thursday");
		categories.add("Firday");
		categories.add("Saturday");
		categories.add("Sunday");

		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

		// Drop down layout style - list view with radio button
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// attaching data adapter to spinner
		spinner.setAdapter(dataAdapter);
	}

	private void setRowIdFromIntent() {
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(RemindersDbAdapter.KEY_ROWID) 
									: null;
			
		}
	}
    
    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close(); 
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper.open(); 
    	setRowIdFromIntent();
		populateFields();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    		case DATE_PICKER_DIALOG: 
    			return showDatePicker();
    		case TIME_PICKER_DIALOG: 
    			return showTimePicker(); 
    	}
    	return super.onCreateDialog(id);
    }
    
 	private DatePickerDialog showDatePicker() {
		
		
		DatePickerDialog datePicker = new DatePickerDialog(ReminderEditActivity.this, new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mCalendar.set(Calendar.YEAR, year);
				mCalendar.set(Calendar.MONTH, monthOfYear);
				mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateDateButtonText(); 
			}
		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)); 
		return datePicker; 
	}

   private TimePickerDialog showTimePicker() {
		
    	TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				HOUR=hourOfDay;
				MIN=minute;
				mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mCalendar.set(Calendar.MINUTE, minute); 
				mCalendar.set(Calendar.SECOND, 0);
				mCalendar.set(Calendar.MILLISECOND, 0);
				updateTimeButtonText();
			}
		}, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true); 
		
    	return timePicker; 
	}
 	
	private void registerButtonListenersAndSetDefaultText() {

		mDateButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DATE_PICKER_DIALOG);  
			}
		}); 
		
		
		mTimeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(TIME_PICKER_DIALOG); 
			}
		}); 
		
		mConfirmButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		saveState(); 
        		setResult(RESULT_OK);
        	    Toast.makeText(ReminderEditActivity.this, getString(R.string.task_saved_message), Toast.LENGTH_SHORT).show();
        	    finish(); 
        	}
          
        });
		
		  updateDateButtonText(); 
	      updateTimeButtonText();
	}
   
    private void populateFields()  {
    	
  	
    	
    	// Only populate the text boxes and change the calendar date
    	// if the row is not null from the database. 
        if (mRowId != null) {
            Cursor reminder = mDbHelper.fetchReminder(mRowId);
            startManagingCursor(reminder);
            mTitleText.setText(reminder.getString(
    	            reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_TITLE)));
            mBodyText.setText(reminder.getString(
                    reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_BODY)));
            

            // Get the date from the database and format it for our use. 
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            Date date = null;
			try {
				String dateString = reminder.getString(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_DATE_TIME)); 
				date = dateTimeFormat.parse(dateString);
	            mCalendar.setTime(date); 
			} catch (ParseException e) {
				Log.e("ReminderEditActivity", e.getMessage(), e); 
			} 
        } else {
        	// This is a new task - add defaults from preferences if set. 
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
        	String defaultTitleKey = getString(R.string.pref_task_title_key); 
        	String defaultTimeKey = getString(R.string.pref_default_time_from_now_key); 
        	
        	String defaultTitle = prefs.getString(defaultTitleKey, null);
        	String defaultTime = prefs.getString(defaultTimeKey, null); 
        	
        	if(defaultTitle != null)
        		mTitleText.setText(defaultTitle); 
        	
        	if(defaultTime != null)
        		mCalendar.add(Calendar.MINUTE, Integer.parseInt(defaultTime));
        	
        }
        
        updateDateButtonText(); 
        updateTimeButtonText(); 
        	
    }

	private void updateTimeButtonText() {
		// Set the time button text based upon the value from the database
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT); 
        String timeForButton = timeFormat.format(mCalendar.getTime()); 
        mTimeButton.setText(timeForButton);
	}

	private void updateDateButtonText() {
		// Set the date button text based upon the value from the database 
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT); 
        String dateForButton = dateFormat.format(mCalendar.getTime()); 
        mDateButton.setText(dateForButton);
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(RemindersDbAdapter.KEY_ROWID, mRowId);
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT); 
    	String reminderDateTime = dateTimeFormat.format(mCalendar.getTime());
		Log.e("test","reminderDateTime==========>>>"+reminderDateTime);
        if (mRowId == null) {
        	
        	long id = mDbHelper.createReminder(title, body, reminderDateTime);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateReminder(mRowId, title, body, reminderDateTime);
        }
       
        new ReminderManager(this).setReminder(mRowId,title,body,reminderDateTime, mCalendar);
    }
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// On selecting a spinner item
		String item = parent.getItemAtPosition(position).toString();
		String set_day="";

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		String reminderDateTime = dateTimeFormat.format(mCalendar.getTime());
		//mCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		Log.e("test","inti ==========>>>"+reminderDateTime);
		switch (position){
			case 0:
				mCalendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
				mCalendar.set(Calendar.HOUR_OF_DAY,0);
				set_day="MONDAY";
				break;
			case 1:
				mCalendar.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
				mCalendar.set(Calendar.HOUR_OF_DAY,0);
				set_day="TUESDAY";
				break;
			case 2:
				mCalendar.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
				mCalendar.set(Calendar.HOUR_OF_DAY,0);
				set_day="WEDNESDAY";
				break;
			case 3:
				mCalendar.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
				mCalendar.set(Calendar.HOUR_OF_DAY,0);
				set_day="THURSDAY";
				break;
			case 4:
				mCalendar.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
				mCalendar.set(Calendar.HOUR_OF_DAY,0);
				set_day="FRIDAY";
				break;
			case 5:
				mCalendar.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
				mCalendar.set(Calendar.HOUR_OF_DAY,0);
				set_day="SATURDAY";
				break;
			case 6:
				mCalendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
				mCalendar.set(Calendar.HOUR_OF_DAY,0);
				set_day="SUNDAY";
				break;
		}
		String current_Time = dateTimeFormat.format(Calendar.getInstance().getTime());

		//Log.e("test"," current_Time ==========>>>"+current_Time);

		mCalendar.set(Calendar.HOUR_OF_DAY,HOUR);
		mCalendar.set(Calendar.MINUTE,MIN);
		mCalendar.set(Calendar.SECOND, 0);

		String calnder = dateTimeFormat.format(mCalendar.getTime());
		//Log.e("test"," calnder ==========>>>"+calnder);
		if (mCalendar.before(Calendar.getInstance())) {
			mCalendar.add(Calendar.DATE,7);
		}else {

		}
		reminderDateTime = dateTimeFormat.format(mCalendar.getTime());
		mTitleText.setText("Every "+set_day+" "+reminderDateTime);

		Log.e("test"," set alarm time ==========>>>"+reminderDateTime);
		// Showing selected spinner item
		Toast.makeText(parent.getContext(), "Selected: " + set_day, Toast.LENGTH_LONG).show();
	}
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
	private void calanderTimeNextDay() {
		Calendar c=Calendar.getInstance();
		c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
		c.set(Calendar.HOUR_OF_DAY,0);
		//c.set(Calendar.MINUTE,0);
		//c.set(Calendar.SECOND,0);
		c.setTimeInMillis(System.currentTimeMillis());
		DateFormat df=new SimpleDateFormat("EEE yyyy/MM/dd HH:mm:ss");

		// This past Sunday [ May include today ]
		if (c.before(Calendar.getInstance())) {
			c.add(Calendar.DATE,7);
		}else {
			Log.e("test","===>"+df.format(c.getTime()));
		}


		Log.e("test","===>"+df.format(c.getTime()));
// Next Sunday
		c.add(Calendar.DATE,7);

		Log.e("test","===>"+df.format(c.getTime()));
		c.add(Calendar.DATE,7);
		Log.e("test","===>"+df.format(c.getTime()));
		c.add(Calendar.DATE,7);
		Log.e("test","===>"+df.format(c.getTime()));
	}
}
