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
import java.util.TreeMap;

import android.content.Context;
import android.text.format.Time;

/**
 * Schedule holds the days and times that the ringer mode will be changed. It
 * gets serialized when the service is closed and deserialized when it starts 
 * again in order to retain the schedule across restarts.
 * 
 * When adding scheduled times it is import that the date is the epoch date of 
 * 01/01/1970 and the seconds should be 00.
 */
public class Schedule implements java.io.Serializable {

	/**
	 *An array of TreeMaps who's indexes mimic the days of the week as set up
	 * in class android.text.format.Time. Each array index is the day it
	 * represents.
	 * 
	 * @example: 0 = Sunday, 1 = Monday etc....
	 */
	private ArrayList<TreeMap<Long, Integer>> mWeek;
	private static final long serialVersionUID = 1L;
	private final String BAD_TIME =
		"The specified time is not a scheduled ring mode change.";

	public static final String FILENAME = "schedule.obj";
	public static final String SCHEDULED_TIME = "SCHEDULED_TIME";
	public static final String RINGER_MODE = "RINGER_MODE";
	public static final String SCHEDULE_DOW = "SCHEDULE_DOW";

	public String getSchedulesFileNameOnDisk() {
		return FILENAME;
	}

	public ArrayList<TreeMap<Long, Integer>> getWeek() {
		return mWeek;
	}

	public TreeMap<Long, Integer> getDay(int dayOfWeek) {
		return mWeek.get(dayOfWeek);
	}

	public Schedule() {
		int nbrDaysInWeek = 7;
		mWeek = new ArrayList<TreeMap<Long, Integer>>(nbrDaysInWeek);
		for (int i = 0; i < nbrDaysInWeek; i++) {
			mWeek.add(i, new TreeMap<Long, Integer>());
		}
	}

	/**
	 * Add a ring change time and mode to the schedule.
	 * 
	 * @param ringerMode An integer that represents the android <b>AudioManager
	 * </b> ringer mode constants silent, vibrate or ring.
	 * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
	 * 6=Saturday.
	 * @param startTime The time in millis to change the ringer mode
	 * @throws IllegalArgumentException thrown when the startTime is already 
	 * present in the schedule
	 */
	public void addRingSchedule(int ringerMode, int dayOfWeek, Long startTime)
			throws IllegalArgumentException {
		Time t = new Time();
		t.set(startTime);
		if (mWeek.get(dayOfWeek).containsKey(startTime))
			throw new IllegalArgumentException(
					"A ring mode change is already scheduled for that time");
		mWeek.get(dayOfWeek).put(startTime, ringerMode);
	}

	/**
	 * Delete a scheduled ring mode change.
	 * 
	 * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
	 * 6=Saturday.
	 * @param startTime The time to delete from the schedule
	 * @throws IllegalArgumentException if the schedule does not contain the 
	 * specified time.
	 */
	public void delRingSchedule(int dayOfWeek, Long startTime)
			throws IllegalArgumentException {
		if (mWeek.get(dayOfWeek).containsKey(startTime)) {
			mWeek.get(dayOfWeek).remove(startTime);
		} else {
			throw new IllegalArgumentException(BAD_TIME);
		}
	}

	/**
	 * This method clears the entire ring schedule.
	 */
	public void delEntireSchedule() {
		for (int i = 0; i < mWeek.size(); i++) {
			mWeek.get(i).clear();
		}
	}

	/**
	 * This method changes the ring mode to change to for the time passed in 
	 * as startTime
	 * 
	 * @param newRingMode An integer that represents the android <b>AudioManager
	 * </b> ringer mode constants silent, vibrate or ring.
	 * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
	 * 6=Saturday.
	 * @param startTime The time in millis to change the ringer mode
	 * @throws IllegalArgumentException thrown when the startTime is already 
	 * present in the schedule
	 */
	public void updateRingSchedule(int dayOfWeek, Long startTime,
			int newRingMode) throws IllegalArgumentException {
		if (hasTime(dayOfWeek, startTime)) {
			delRingSchedule(dayOfWeek, startTime);
			addRingSchedule(newRingMode, dayOfWeek, startTime);
		} else {
			throw new IllegalArgumentException(BAD_TIME);
		}
	}

	/**
	 * Check to see if the schedule contains the indicated time.
	 * 
	 * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
	 * 6=Saturday.
	 * @param startTime The time to delete from the schedule
	 * @return A boolean that returns true if the schedule has the indicated 
	 * time and false if it does not.
	 */
	public boolean hasTime(int dayOfWeek, Long startTime) {
		return mWeek.get(dayOfWeek).containsKey(startTime);
	}

	public int getRingerMode(int dayOfWeek, Long startTime) {
		if(!hasTime(dayOfWeek, startTime))
			throw new IllegalArgumentException(BAD_TIME);
		return mWeek.get(dayOfWeek).get(startTime);
	}

	public static Schedule loadSchedule(Context context) throws IOException,
			ClassNotFoundException {
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			fis = context.openFileInput(FILENAME);
			ois = new ObjectInputStream(fis);
			return (Schedule) ois.readObject();
		} catch (IOException ex) {
			throw ex;
		} catch (ClassNotFoundException ex) {
			throw ex;
		} finally {
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
		} finally {
			fos.close();
			oos.close();
		}

	}
	
}
