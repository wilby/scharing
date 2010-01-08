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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class Scheduler extends Activity {
    
	private final String TAG = "Scharing_Scheduler";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	    	
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);
        
        Intent servIntent = new Intent(this, net.wcjj.scharing.Service.class);
    	startService(servIntent);   
    	 	
    	InitializeUI();
    }
    
    public void InitializeUI() {
    	InitializeSpinners();
    }
    
    public void InitializeSpinners() {
    	Spinner spWeekday = (Spinner) findViewById(R.id.spWeekday);
    	ArrayAdapter<CharSequence> adpWeekday = ArrayAdapter.createFromResource(
    	            this, R.array.weekday, android.R.layout.simple_spinner_item);
    	adpWeekday.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spWeekday.setAdapter(adpWeekday);	        
	    
	    Spinner spRingerMode = (Spinner) findViewById(R.id.spRingerMode);
    	ArrayAdapter<CharSequence> adpRingerMode = ArrayAdapter.createFromResource(
    	            this, R.array.ringer_mode, android.R.layout.simple_spinner_item);
    	adpRingerMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spRingerMode.setAdapter(adpRingerMode);
    }
    
    
    @Override
	protected void onPause() {
    	try {
			Service.RingSchedule.saveSchedule(this);
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		super.onPause();
	}
    
    
   
    
    //Listeners
    
    public void btnAddRingerChangeSchedule_Click(View v) {    	
    	int weekday,ringerMode;
    	String startTime;    	
       	
    	Spinner spWeekday = (Spinner)findViewById(R.id.spWeekday);
    	TimePicker tpStartTime = (TimePicker) findViewById(R.id.tpTime);
    	Spinner spRingerMode = (Spinner)findViewById(R.id.spRingerMode);
    	
    	weekday = spWeekday.getSelectedItemPosition();
    	startTime = String.valueOf(tpStartTime.getCurrentHour()) 
    				+ ":" 
					+ (String.valueOf(tpStartTime.getCurrentMinute()).length() == 1 ? "0" 
							+ tpStartTime.getCurrentMinute().toString() : tpStartTime.getCurrentMinute().toString());

    	ringerMode = spRingerMode.getSelectedItemPosition();
    	
    	final ScrollView svMessages = (ScrollView)findViewById(R.id.svMessages); 
    	final TextView tvMessages = (TextView)findViewById(R.id.tvMessages);
    	String messageSeperator = getString(R.string.message_seperator);
    	
    	if(!Service.RingSchedule.hasTime(weekday, startTime)) {
    		Service.RingSchedule.addRingSchedule(ringerMode, weekday, startTime);
    		tvMessages.setTextColor(R.color.font_highlight);    		     		
        	tvMessages.append(getString(R.string.ring_added) + messageSeperator);
    	}
    	else {
    		Service.RingSchedule.updateRingSchedule(weekday, startTime, ringerMode);
    		tvMessages.setTextColor(R.color.font_attention);
        	tvMessages.append(getString(R.string.ring_update) + messageSeperator);
    	}
    	
    	/**
    	 * After experimenting awhile scrolling to the point of the last text
    	 * entry was supported with the following code. Although I'm sure 
    	 * there is a way to do this with the textview alone I have yet to 
    	 * figure it out. The docs are unclear on how scrolling is expected 
    	 * to be used on the TextView.
    	 */
    	svMessages.post(new Runnable() {
    	    public void run() {
    	        svMessages.scrollTo(0, tvMessages.getHeight());
    	    }
    	});
    	
    	
		/*tvMessages.setMovementMethod(ScrollingMovementMethod.getInstance());
		tvMessages.post(new Runnable() {			
			public void run() {
				tvMessages.scrollTo(0, tvMessages.getBottom());
				
			}
		});*/  	
    	
    }
    
    public void btnViewSchedules_Click(View v) {    	
    	Intent i = new Intent(this, ScheduleDays.class);
    	startActivity(i);    	
    }
    
    
//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//      // Save UI state changes to the savedInstanceState.
//      // This bundle will be passed to onCreate if the process is
//      // killed and restarted.
//      savedInstanceState.putBoolean("MyBoolean", true);
//      savedInstanceState.putDouble("myDouble", 1.9);
//      savedInstanceState.putInt("MyInt", 1);
//      savedInstanceState.putString("MyString", "Welcome back to Android");
//      // etc.
//      super.onSaveInstanceState(savedInstanceState);
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//      super.onRestoreInstanceState(savedInstanceState);
//      // Restore UI state from the savedInstanceState.
//      // This bundle has also been passed to onCreate.
//      boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
//      double myDouble = savedInstanceState.getDouble("myDouble");
//      int myInt = savedInstanceState.getInt("MyInt");
//      String myString = savedInstanceState.getString("MyString");
//    }
    
}

