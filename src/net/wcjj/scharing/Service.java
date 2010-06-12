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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import java.io.IOException;


//LEAVE ALL IMPORTS, THEY WILL BE USED WITH CALENDAR FUNCTIONALITY

/**
 * This class reads from the Schedule (set by the user) and changes the ring mode 
 * based on the users preferences at a given time of day. This class needs to run 
 * constantly in the background. It is started on device boot or when the UI is started. 
 **/
public class Service extends android.app.Service implements IScharingPreferences {
	
	private AudioManager mAudioManager;	
	private final String TAG = "Scharing_Service";	
	private boolean mShowAlerts;
	private static Schedule mRingSchedule;
		
	public static Schedule getRingSchedule() {
		return mRingSchedule;
	}		
	
	@Override
	public void onCreate() {				
		registerReceiver(new TimeTickListener(),new IntentFilter(Intent.ACTION_TIME_TICK));		
		registerReceiver(new ScharingSetAlertsListener(), new IntentFilter(ScharingIntents.HIDE_SHARING_ALERTS));
		registerReceiver(new ScharingSetAlertsListener(), new IntentFilter(ScharingIntents.SHOW_SHARING_ALERTS));
		
		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);	
	
		loadRingSchedule();
		loadPreferences();		
	}	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.i(TAG, TAG + " started.");
	    // Continue running until it is explicitly stopped.
	    return START_STICKY;
	}
	
	@Override
	public void onDestroy()  {
		  //Save our schedule if the services is killed, this is not working.
	      //saveRingSchedule();
	      savePreferences();
	      //Let the user know that their next schedule ringer mode
	      //change will not occur.
	  	  Utilities.scharingNotification(getApplicationContext(), 
	  	       	getString(R.string.service_shutdown_warning));
	  	  
		mRingSchedule = null;
		mAudioManager = null;		
		super.onDestroy();
	}	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private void showRingChangeAlert() {
		if(mShowAlerts)
			Utilities.scharingNotification(getApplicationContext(), 
					getString(R.string.ring_mode_changed) 
					+ " " + Utilities.RINGER_MODES_TEXT[mAudioManager.getRingerMode()] + " @: ");	
	}		
	
	/**
	 * De-serialize the schedule object from disk.
	 */
	private void loadRingSchedule() {
		try {
			boolean fileNameMatches = false;
			String schedulesFileName = "schedule.obj";
			String[] fileNames = fileList();
			if(fileNames.length > 0)
				for (int i = 0; i < fileNames.length; i++) {
					fileNameMatches = fileNames[i] == schedulesFileName;
					if(fileNameMatches)
						break;
				}
			
			if(fileNameMatches) { 						
				mRingSchedule = Schedule.loadSchedule(this);		
			}
			else {
				//First run create and persists schedule
				mRingSchedule = new Schedule();
				mRingSchedule.saveSchedule(this);
			}						
		} 
		catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (ClassNotFoundException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}		
	}		    
    
    //IScharingPreferences implementation    
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
	
    //Broadcast Receivers
	/**
	*Respond to system time advancements, occurs every one minute.
	*If there is a matching day/time in the schedule then change the ringer 
	*mode to the mode specified in the schedule.
	**/
	public class TimeTickListener extends BroadcastReceiver {	
		@Override
		public void onReceive(Context context, Intent intent) {
			long millis = System.currentTimeMillis();			
								
			String strTime = Utilities.toScheduleTimeFormat(
						   millis);
			Time t = new Time();
			t.set(millis);
			int weekday = t.weekDay;
			if(mRingSchedule.hasTime(weekday, strTime)) {					
				mAudioManager.setRingerMode(mRingSchedule.getRingerMode(
								      weekday, strTime));
				showRingChangeAlert();
			}
		}
	}

	/**
	 *Respond to intents that are being broadcast to let us know that  
	 *the user has changed their show alerts preference. 
	 */
	public class ScharingSetAlertsListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			mShowAlerts = intent.getAction().equals(ScharingIntents.SHOW_SHARING_ALERTS) ? true :  false;			
		}	
	}
	
	/**
	 * Listen for request of the state of showing scharing alerts (REQUEST_SHARING_ALERTS_STATE). If a broadcast is 
	 * recieved respond with one of two intents letting the requester know true or false
	 */
	public class ShowAlertsStateListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(mShowAlerts) {
				sendBroadcast(new Intent(ScharingIntents.SHOW_SHARING_ALERTS));
			}
			else {
				sendBroadcast(new Intent(ScharingIntents.HIDE_SHARING_ALERTS));		
			}
		}	
	}
	
	
}



