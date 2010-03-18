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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import android.content.Context;

/**
 * Schedule holds the days and times that the ringer mode will be changed.
 * It gets serialized when Service is closed and deserialized when it starts
 * again in order to retain the schedule across restarts.
 */
public class Schedule implements java.io.Serializable {
	
	
	
	/**
	*An array of HashMaps who's indexes mimic the days of the week 
	*as set up in class android.text.format.Time. Each 
	*array index is the day it represents.
	*@example:
	*	0 = Sunday, 1 = Monday etc....
	*/
	private ArrayList<HashMap<String, Integer>> mWeek ;
	private static final long serialVersionUID = 1L;
		
	
	
	public static final String FILENAME = "schedule.obj";
	public static final String SCHEDULED_TIME = "SCHEDULED_TIME";
	public static final String RINGER_MODE = "RINGER_MODE";
	public static final String SCHEDULE_DOW = "SCHEDULE_DOW";
	
	
	public String getSchedulesFileNameOnDisk() {
		return FILENAME;
	}
	
	public ArrayList<HashMap<String, Integer>> getWeek() {
		return mWeek;		
	}
	
	
	
	public HashMap<String, Integer> getDay(int dayOfWeek) {
		return mWeek.get(dayOfWeek);
	}
	
	
	
	public Schedule() {	
        int nbrDaysInWeek = 7;
		mWeek = new ArrayList<HashMap<String, Integer>>(nbrDaysInWeek);
		for(int i = 0; i < nbrDaysInWeek; i++) {
			mWeek.add(i, new HashMap<String, Integer>());
		}
	}
	
	
	
	public void addRingSchedule(int ringerMode, int dayOfWeek, String startTime)
	    throws IllegalArgumentException {
		if (mWeek.get(dayOfWeek).containsKey(startTime))
			throw new IllegalArgumentException(
				  "A ring mode change is already scheduled for that time");
		mWeek.get(dayOfWeek).put(startTime, ringerMode);	
	}
	
	

	public void delRingSchedule(int dayOfWeek, String startTime) throws IllegalArgumentException {
		if(mWeek.get(dayOfWeek).containsKey(startTime)) {
			mWeek.get(dayOfWeek).remove(startTime);
		}
		else {
			throw new IllegalArgumentException(startTime +
							   " is not a scheduled ring mode change.");
		}
	}
	
	public void delEntireSchedule() {		
		for(int i = 0; i < mWeek.size(); i++ ) {
			mWeek.get(i).clear();
		}
	}
	
	public void updateRingSchedule(int dayOfWeek, String startTime, int newRingMode)
	    throws IllegalArgumentException {
		if(mWeek.get(dayOfWeek).containsKey(startTime)) {			
			delRingSchedule(dayOfWeek, startTime);
			addRingSchedule(newRingMode, dayOfWeek, startTime);
		}	
		else {
			throw new IllegalArgumentException(startTime + 
							   " is not a scheduled ring mode change.");
		}
	}
	
	
	
	public boolean hasTime(int dayOfWeek, String startTime) {
		return mWeek.get(dayOfWeek).containsKey(startTime);
	}
	
	
	
	public int getRingerMode(int dayOfWeek, String startTime) {		
		return mWeek.get(dayOfWeek).get(startTime);			
	}
	
	
	
	public static Schedule loadSchedule(Context context) throws IOException,ClassNotFoundException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		
		try {
			fis = context.openFileInput(FILENAME);
			ois = new ObjectInputStream(fis);
			return (Schedule)ois.readObject();
		}
		catch(IOException ex) {
			throw ex;
		}
		catch(ClassNotFoundException ex) {
			throw ex;
		}
		finally {
			fis.close();
			ois.close();
		}
	}
	
	
	
	 public void saveSchedule(Context context) throws IOException {
	    	
			FileOutputStream fos = null;
			ObjectOutputStream oos = null;
			
			try {
				fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(this);			
			} catch (IOException ex) {			
				throw ex;
			}		
			finally {
				fos.close();
				oos.close();
			}
	    	
    }
	 
	 
	
	/**
	 * I could have extended a ListAdapter and created a way to 
	 * handle Schedule as it occurs in this class. I want to 
	 * get this app to beta asap so I can start using it so 
	 * I am cheating and utilizing a quick fix by converting 
	 * the schedule to a format that the SimpleAdapter can 
	 * handle out of the box.
	 * 
	 * @param day The int value for the day of the week to return the Map
	 * @return
	 */
	public ArrayList<HashMap<String, String>> toSimpleAdapterMap(int day) {		
		String[] ringModes = Utilities.RINGER_MODES_TEXT;		
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
		ArrayList<HashMap<String, Integer>> week = mWeek;		
		
		//Hashmaps are not sorted, utilizing collection sorting so that 
		//items are displayed in time asending order in the days listview.
		Vector<String> v = new Vector<String>(week.get(day).keySet());
	    Collections.sort(v);
	    Iterator<String> it = v.iterator();
	    
	    while (it.hasNext()) {
	       String key =  (String)it.next();
	       HashMap<String, String> tempMap = new HashMap<String, String>(3);
			tempMap.put(SCHEDULE_DOW, String.valueOf(day));
			tempMap.put(SCHEDULED_TIME, key);
			tempMap.put(RINGER_MODE, ringModes[week.get(day).get(key)]);
			list.add(tempMap);			
	    }	
		
		return list;
	}
	
	
	
}

