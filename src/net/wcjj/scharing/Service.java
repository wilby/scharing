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
import android.media.AudioManager;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import java.io.IOException;

/**
 * This class reads from the a Schedule (set by the user) and changes the ring mode 
 * based on the users preferences at a given time of day. This class needs to run 
 * constantly in the background. It is started on device boot or when the UI is started.
 **/
public class Service extends android.app.Service {
	
	
	
	private AudioManager mAudioManager;
	private static Schedule mRingSchedule;	
	private final String TAG = "Scharing_Service";
	
	
	
	public static Schedule getRingSchedule() {
		return mRingSchedule;
	}
	
	
	
	@Override
	public void onCreate() {				
		registerReceiver(new TimeTickListener(),new IntentFilter(Intent.ACTION_TIME_TICK));
		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		
		try {
			//schedule is the only file we create so if its not there 
			//then create one and shelve it.
			if(fileList().length > 0) { 						
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
			// TODO Auto-generated catch block
			Log.e(TAG, Log.getStackTraceString(e));
		}
		
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.i(TAG, TAG + " started.");
	    // Continue running until it is explicitly stopped.
	    return START_STICKY;
	}	
	
	
	
	/**
	 * Save the schedule and let the user know that their next 
	 * ringer mode change will not occur.
	 */
    public void save() {
    	  try {
  	       	mRingSchedule.saveSchedule(this);
  	       	Utilities.scharingNotification(getApplicationContext(), 
  	       	getString(R.string.service_shutdown_warning));
  	    } catch (IOException e) {
  	       	// TODO Auto-generated catch block			
  	       	Log.e(TAG, e.getMessage());
  	    }
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
	*Respond to system time advancements, occurs every one minute.
	*If there is a matching day/time in the schedule then change the ringer 
	*mode to the mode in the schedule.
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
				Utilities.scharingNotification(getApplicationContext(), 
					       getString(R.string.ring_mode_changed) + Utilities.RINGER_MODES_TEXT[mAudioManager.getRingerMode()] + " @: ");
			}
			t = null;
				
		}
	}
	
	

}



