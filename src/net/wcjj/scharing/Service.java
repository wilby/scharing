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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * This class reads from the mSchedule (set by the user) and changes the ring mode 
 * based on the users preferences at a given time of day.
*/
public class Service extends android.app.Service {
	
	private String mFilename = "schedule.obj";
	private AudioManager mAudioManager;
	public static Schedule mSchedule;
	public static boolean mEnabled;

	private final String TAG = "Scharing_Service";
	
	
	
	@Override
	public void onCreate() {				
		registerReceiver(new TimeTickListener(),new IntentFilter(Intent.ACTION_TIME_TICK));
		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		
		try {
			if(fileList().length > 0) { 						
				mLoadSchedule();		
			}
			else {
				mSchedule = new Schedule();
				mSaveSchedule();
			}
			
		} 
		catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.i(TAG, TAG + " started.");
	    // Continue running until it is explicitly stopped.
	    return START_STICKY;
	}
	
	
	
	@Override
	public void onDestroy() {	
		try {
			mSaveSchedule();
			Utilities.scharingNotification(getApplicationContext(), "Scharing shut down, next ringer change will not occur.");
		} catch (IOException e) {
			// TODO Auto-generated catch block			
			Log.e(TAG, e.getMessage());
		}
		
	}
	
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	private void mSaveSchedule() throws IOException {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		
		try {
			fos = openFileOutput(mFilename, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(mSchedule);			
		} catch (IOException ex) {			
			throw ex;
		}		
		finally {
			oos.close();
		}
	}
	
	
	
	private void mLoadSchedule() throws IOException,ClassNotFoundException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		
		try {
			fis = openFileInput(mFilename);
			ois = new ObjectInputStream(fis);
			mSchedule = (Schedule)ois.readObject();
		}
		catch(IOException ex) {
			throw ex;
		}
		catch(ClassNotFoundException ex) {
			throw ex;
		}
		finally {
			ois.close();
		}
	}
	
	
	
	//Respond to system time advancements, occurs every one minute.
	public class TimeTickListener extends BroadcastReceiver {	
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String strTime = Utilities.toScheduleTimeFormat(System.currentTimeMillis());	
				int weekday = Time.WEEK_DAY;
				if(mSchedule.hasTime(weekday, strTime)) {
					Utilities.scharingNotification(getApplicationContext(), String.valueOf(weekday) + 
							strTime);
					mAudioManager.setRingerMode(mSchedule.getRingerMode(weekday, strTime));
					
					mSaveSchedule();
				}				
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}
	}

} // End class



