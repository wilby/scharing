/**
* 	 Scharing - Allows you to set a ring, vibrate and silence shedule for your android device.
*    Copyright (C) 2009  Wilby C. Jackson Jr.
*
*    This program is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation; either version 2 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License along
*    with this program; if not, write to the Free Software Foundation, Inc.,
*    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*    
*    Contact: jacksw02-at-gmail-dot-com
*/

package net.wcjj.scharing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;


public class SchedulerUI extends Activity implements IScharingPreferences {
    	
	private final String TAG = "SCHARING_SCHEDULER";	
	private boolean mShowAlerts;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {    	    	
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.scheduler_ui);
        registerReceiver(new ScharingShowAlertsStateListener(), new IntentFilter(ScharingIntents.HIDE_SHARING_ALERTS));
		registerReceiver(new ScharingShowAlertsStateListener(), new IntentFilter(ScharingIntents.SHOW_SHARING_ALERTS));
		
        //Start the Scharing service.
        Intent servIntent = new Intent(this, net.wcjj.scharing.Service.class);
    	startService(servIntent);   
	 	
    	//Populate the spinners in the UI
    	InitializeSpinners();    	
    	loadPreferences();
    }        
        
    @Override
	protected void onPause() {
    	//Make sure that user changes are saved when the UI focus is lost
    	try {
			Service.getRingSchedule().saveSchedule(this);
			savePreferences();
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		super.onPause();
	}  
        
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedulerui_menu, menu);		
		
		if (mShowAlerts) {
			menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.checkedbox));
		}
		else {
			menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.uncheckedbox));
		}
		return true;
	}    
    
    public class ScharingShowAlertsStateListener extends BroadcastReceiver {    
    	@Override
		public void onReceive(Context context, Intent intent) {
    		mShowAlerts = intent.getAction().equals(ScharingIntents.SHOW_SHARING_ALERTS) ? true :  false;    		    	    
    	}
    
    } 
    
    /**
     * This event is run when a menu item is clicked on the options (icon) menu
     * 
     * The options menu can not display a checkbox so we must change out the displayed
     * icon to give the effect of checked/unchecked.
     * 
     * Options are saved in a Properties file to disk when they are changed.
     */
    public boolean onOptionsItemSelected(MenuItem item) {    
    	final int SHOW_ALERTS_ID = R.id.chbShowAlerts;
    	final int SCHEDULERUI_HELP_ID = R.id.btnShedulerUIHelp;
        switch (item.getItemId()) {
        case SHOW_ALERTS_ID:        	
        	if (item.isChecked()) {
        		item.setChecked(false);
    			item.setIcon(getResources().getDrawable(R.drawable.uncheckedbox));   		
    			sendBroadcast(new Intent(ScharingIntents.HIDE_SHARING_ALERTS));
    			savePreferences();
    		}
    		else {
    			item.setChecked(true);
    			item.setIcon(getResources().getDrawable(R.drawable.checkedbox));    			
    			sendBroadcast(new Intent(ScharingIntents.SHOW_SHARING_ALERTS));
    			savePreferences();
    		}        	
            return true; 
    	case SCHEDULERUI_HELP_ID:
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(R.string.schedulerui_helpdialog_title)
        			.setMessage(getString(R.string.schedulerui_helpdialog_message))
        	       .setCancelable(false)
        	       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create(); 
        	alert.show();      	
            return true;        
        }
        return false;
    }  
        
    /**
     * Setup databinding for the spinners on the ui.
     */
    public void InitializeSpinners() {
    	Spinner spWeekday = (Spinner) findViewById(R.id.spWeekday);
    	ArrayAdapter<CharSequence> adpWeekday = ArrayAdapter.createFromResource(
    	            this, R.array.weekday, android.R.layout.simple_spinner_item);    	
    	
    	adpWeekday.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spWeekday.setAdapter(adpWeekday);
	    
	    //Make the selected day default to today
	    Time t = new Time();
		t.set(System.currentTimeMillis());
    	spWeekday.setSelection(t.weekDay);
	    
	    
	    Spinner spRingerMode = (Spinner) findViewById(R.id.spRingerMode);
    	ArrayAdapter<CharSequence> adpRingerMode = ArrayAdapter.createFromResource(
    	            this, R.array.ringer_mode, android.R.layout.simple_spinner_item);
    	adpRingerMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spRingerMode.setAdapter(adpRingerMode);	    
	    
	    
    }   
    
    
    /**
     * Convenience method for adding or updating times to the days of the week schedule
     * 
     * @param ringerMode The mode to change to from
     * @param weekday The day of the week (Mon-Fri) the ringer change should occur 
     * @param startTime The time the ring mode should change.
     * @param tvMessages The message to display to the user.
     */
    private void addSchedules(int ringerMode, int weekday, String startTime, TextView tvMessages) {
    	if(Service.getRingSchedule().hasTime(weekday, startTime)) {
    		updateSchedules(ringerMode, weekday, startTime, tvMessages);
    	}
    	else {
	    	String messageSeperator = getString(R.string.message_seperator);
	    	Service.getRingSchedule().addRingSchedule(ringerMode, weekday, startTime);			  		
	    	tvMessages.append(getString(R.string.ring_added) + messageSeperator);
    	}
    }
    
    
    /**
     * Convenience method for updating an existing ring schedule to a different ringer mode.
     * 
     * @param ringerMode The mode to change to from
     * @param weekday The day of the week (Mon-Fri) the ringer change should occur 
     * @param startTime The time the ring mode should change.
     * @param tvMessages The message to display to the user.
     */
    private void updateSchedules (int ringerMode, int weekday, String startTime, TextView tvMessages) {
    	String messageSeperator = getString(R.string.message_seperator);
    	Service.getRingSchedule().updateRingSchedule(weekday, startTime, ringerMode);		
    	tvMessages.append(getString(R.string.ring_update) + messageSeperator);
    }
   
    
    
    //Listeners    
    
    public void btnAddRingerChangeSchedule_Click(View v) {    	
    	int weekday,ringerMode;
    	String startTime;    	
       	
    	
    	//Grab the views on the ui.
    	Spinner spWeekday = (Spinner)findViewById(R.id.spWeekday);
    	TimePicker tpStartTime = (TimePicker) findViewById(R.id.tpTime);
    	Spinner spRingerMode = (Spinner)findViewById(R.id.spRingerMode);
    	
    	tpStartTime.requestFocus();
    	
    	weekday = spWeekday.getSelectedItemPosition();
    	startTime = Utilities.toScheduleTimeFormat(tpStartTime);
    	ringerMode = spRingerMode.getSelectedItemPosition();
    	
    	final ScrollView svMessages = (ScrollView)findViewById(R.id.svMessages); 
    	final TextView tvMessages = (TextView)findViewById(R.id.tvMessages);
    	
    	
    	
    	/**
    	 * Convenience for the user. If they select Mon-Fri in spinner then we
    	 * add the schedule time to all weekdays. If weekends then Sat-Sun. 
    	 */   	
    	if (weekday == Utilities.WEEKDAYS) {
    		int mon = 1;
    		int fri = 5;
    		for(int i = mon; i <= fri; i++)    			
    			addSchedules(ringerMode, i, startTime, tvMessages);    		
    	}	
    	else if (weekday == Utilities.WEEKENDS) {
    		int sat = 6;
    		int sun = 0;
    		addSchedules(ringerMode, sat, startTime, tvMessages);
    		addSchedules(ringerMode, sun, startTime, tvMessages);    		
    	}
    	else {
    		addSchedules(ringerMode, weekday, startTime, tvMessages);    	
    	}
    	
    	
    	
    	/**
    	 * After experimenting awhile scrolling to the point of the last text
    	 * entry was supported with the following code. Although I'm sure 
    	 * there is a way to do this with the textview alone I have yet to 
    	 * figured it out. The docs are unclear on how scrolling is expected 
    	 * to be used for the TextView.
    	 */
    	svMessages.post(new Runnable() {
    	    public void run() {
    	        svMessages.scrollTo(0, tvMessages.getHeight());
    	    }
    	});    
    	
    }
    
    
    
    /**
     * Open the Week ui when the user clicks the View Schedule button. 
     * 
     * @param v
     */
    public void btnViewSchedules_Click(View v) { 
     	Intent i = new Intent(this, WeekViewDeleteUI.class);
    	startActivity(i);	
    }  
    
    //IScharingPrefernces implementation
    
    public void loadPreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(Utilities.PREFERENCES_FILENAME, MODE_PRIVATE);
        mShowAlerts = settings.getBoolean(Utilities.PREFERENCES_SHOW_ALERTS_VARIABLE_NAME, true); 
    }
    
    public void savePreferences() {
    	    	 
        SharedPreferences settings = getSharedPreferences(Utilities.PREFERENCES_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Utilities.PREFERENCES_SHOW_ALERTS_VARIABLE_NAME, mShowAlerts);        
        editor.commit();
    }
    
}



