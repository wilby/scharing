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
import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;
import java.util.HashMap;

/**
*/
public class Schedule implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String SCHEDULED_TIME = "SCHEDULED_TIME";
	public static final String RINGER_MODE = "RINGER_MODE";
	public static final String SCHEDULE_DOW = "SCHEDULE_DOW";
	
	/**
	*An array of HashMaps that mimic the days of the week 
	*as set up in class android.text.format.Time. Each 
	*array index is the day it represents
	*@example:
	*	0 = Sunday, 1 = Monday etc....
	*/
	private ArrayList<HashMap<String, Integer>> mWeek ;
	/**
	 * @param startTime - The time to change the ring mode. 
	 * @param ringerMode - The int representing the mode to change to. 
	 * These are static constants in the AudioManager class
	 * @param dayOfWeek - The int representing the day of the week in the Time class.
	*/
	
	public ArrayList<HashMap<String, Integer>> getWeek() {
		return mWeek;		
	}
	
	public HashMap<String, Integer> getDay(int dayOfWeek) {
		return mWeek.get(dayOfWeek);
	}		
	
	public Schedule() {	
		//Java can not create a generic array.
		mWeek = new ArrayList<HashMap<String, Integer>>(7);
		for(int i = 0; i < 7; i++) {
			mWeek.add(i, new HashMap<String, Integer>());
		}
	}
	
	public void addRingSchedule(int ringerMode, int dayOfWeek, String startTime) throws IllegalArgumentException {
		if (mWeek.get(dayOfWeek).containsKey(startTime))
			throw new IllegalArgumentException("A ring mode change is already scheduled for that time");
		mWeek.get(dayOfWeek).put(startTime, ringerMode);	
	}

	public void delRingSchedule(int dayOfWeek, String startTime) throws IllegalArgumentException {
		if(mWeek.get(dayOfWeek).containsKey(startTime)) {
			mWeek.get(dayOfWeek).remove(startTime);
		}
		else {
			throw new IllegalArgumentException(startTime + " is not a scheduled ring mode change.");
		}
	}
	
	public void updateRingSchedule(int dayOfWeek, String startTime, int newRingMode) throws IllegalArgumentException {
		if(mWeek.get(dayOfWeek).containsKey(startTime)) {			
			delRingSchedule(dayOfWeek, startTime);
			addRingSchedule(newRingMode, dayOfWeek, startTime);
		}	
		else {
			throw new IllegalArgumentException(startTime + " is not a scheduled ring mode change.");
		}
	}
	
	public boolean hasTime(int dayOfWeek, String startTime) {
		return mWeek.get(dayOfWeek).containsKey(startTime);
	}
	
	public int getRingerMode(int dayOfWeek, String startTime) {		
		return mWeek.get(dayOfWeek).get(startTime);			
	}
	
	/**
	 * I could have extened a ListAdapter and created a way to 
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
		
		for (int j = 0; j < week.get(day).size(); j++) {
			String key = (String) week.get(day).keySet().toArray()[j];
			HashMap<String, String> tempMap = new HashMap<String, String>(3);
			tempMap.put(SCHEDULE_DOW, String.valueOf(day));
			tempMap.put(SCHEDULED_TIME, key);
			tempMap.put(RINGER_MODE, ringModes[week.get(day).get(key)]);
			list.add(tempMap);			
		}
		
		return list;
	}
}

