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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

//LEAVE ALL IMPORTS, THEY WILL BE USED WITH CALENDAR FUNCTIONALITY

/**
 * This class reads from the Schedule (set by the user) and changes the ring mode 
 * based on the users preferences at a given time of day. This class needs to run 
 * constantly in the background. It is started on device boot or when the UI is started. 
 **/
public class Service extends android.app.Service {
	
	private AudioManager mAudioManager;	
	private final String TAG = "Scharing_Service";	
	public static final String APP_PROPERTIES_FILENAME = "app.properties";	
	private static boolean mShowAlerts;
	private static Schedule mRingSchedule;
	
	
	public static Schedule getRingSchedule() {
		return mRingSchedule;
	}
		
	
	@Override
	public void onCreate() {				
		registerReceiver(new TimeTickListener(),new IntentFilter(Intent.ACTION_TIME_TICK));		
		registerReceiver(new ScharingSetAlertsListener(), new IntentFilter(ScharingIntents.HIDE_SHARING_ALERTS));
		registerReceiver(new ScharingSetAlertsListener(), new IntentFilter(ScharingIntents.SHOW_SHARING_ALERTS));
		registerReceiver(new ScharingSetAlertsListener(), new IntentFilter(ScharingIntents.REQUEST_SHOW_ALERTS_STATE));
		
		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);	
			
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
			loadOrCreateUserProperties();
			
		} 
		catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (ClassNotFoundException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.i(TAG, TAG + " started.");
	    // Continue running until it is explicitly stopped.
	    return START_STICKY;
	}	
	
	
	
	@Override
	public void onDestroy()  {		
		mRingSchedule = null;
		mAudioManager = null;		
		super.onDestroy();
	}
	
	
	public void stopService() {
      //Save our schedule if the services is killed
      save();
      mRingSchedule = null;      
	}
		
	
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * Save the schedule and let the user know that their next 
	 * ringer mode change will not occur.
	 */
    private void save() {
    	  try {
    		mRingSchedule.setShowAlerts(mShowAlerts);
  	       	mRingSchedule.saveSchedule(this);  	       	
  	       	Utilities.scharingNotification(getApplicationContext(), 
  	       	getString(R.string.service_shutdown_warning));
  	    } catch (IOException e) {  	       		
  	       	Log.e(TAG, e.getMessage());
  	    }
    }  
	
	private void loadOrCreateUserProperties() {
		mShowAlerts = mRingSchedule.getShowAlerts();
		
		
//		Properties props = new Properties();		
//		FileInputStream fis = null;
//		try {
//			fis  = this.openFileInput(APP_PROPERTIES_FILENAME);
//			props.load(fis);
//			mShowAlerts = Boolean.parseBoolean(props.getProperty("showalerts"));
//		} catch (FileNotFoundException e1) {
//			Log.d(TAG, "Properties file does not exists." , e1);			
//			FileOutputStream fos = null;
//			try {
//				props.setProperty("showalerts", "true");			
//				fos = this.openFileOutput(APP_PROPERTIES_FILENAME, Service.MODE_PRIVATE);
//				props.store(fos, "Initialization of new properties file.");
//				mShowAlerts = Boolean.parseBoolean(props.getProperty("showalerts"));
//				fos.close();				
//			} catch (IOException ex) {			
//				Log.d(TAG, "Could not create properties file", ex);				
//			}
//			
//		} catch (IOException e1) {			
//			Log.d(TAG, "IO Error while loading properties file." , e1);
//			mShowAlerts = true;
//		}		
	}
	
	
	
	
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

	public class ScharingSetAlertsListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			mShowAlerts = intent.getAction().equals(ScharingIntents.SHOW_SHARING_ALERTS) ? true :  false;			
		}	
	}
	
	/**
	 * Listen for request of the state of showing scharing alerts. If a broadcast is 
	 * recieved respond with on of two intents letting the requester know true or false
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
	
	private void showRingChangeAlert() {
		if(mShowAlerts)
			Utilities.scharingNotification(getApplicationContext(), 
					getString(R.string.ring_mode_changed) 
					+ " " + Utilities.RINGER_MODES_TEXT[mAudioManager.getRingerMode()] + " @: ");
	
	}
}



