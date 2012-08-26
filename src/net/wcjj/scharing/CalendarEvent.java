/**
 * 	 Scharing - Allows you to set a ring, vibrate and silence shedule for your android device.
 *    Copyright (C) 20090   Wilby C. Jackson Jr.
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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarEvent {

	private boolean mAllDay;
	private Date mBeginTime;
	private Date mEndTime;
	private String mDescription;

	private int mBeginRingMode;
	private int mEndRingMode;
	private final String mPatternString;
	private final Pattern mPattern;
	private Matcher mMatcher;
	private boolean mMatchFound;
	private int mNbrMatchesFound;

	public boolean isAllDay() {
		return mAllDay;
	}

	public void setAllDay(boolean allDay) {
		this.mAllDay = allDay;
	}

	public Date getBeginTime() {
		return mBeginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.mBeginTime = beginTime;
	}

	public Date getEndTime() {
		return mEndTime;
	}

	public void setEndTime(Date endTime) {
		this.mEndTime = endTime;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
		mNbrMatchesFound = 0;
	}

	public int getBeginRingMode() {
		return mBeginRingMode;
	}

	public void setBeginRingMode(int mBeginRingMode) {
		this.mBeginRingMode = mBeginRingMode;
	}

	public int getEndRingMode() {
		return mEndRingMode;
	}

	public void setEndRingMode(int mEndRingMode) {
		this.mEndRingMode = mEndRingMode;
	}

	private CalendarEvent() {
		mMatchFound = false;
		mNbrMatchesFound = 0;
		mBeginRingMode = -1;
		mEndRingMode = -1;
		mPatternString = "<[rvs]{1}+,[rvs]{1}+>";
		mPattern = Pattern.compile(mPatternString, Pattern.CASE_INSENSITIVE
				| Pattern.MULTILINE);

	}

	public CalendarEvent(Date beginTime, Date endTime, boolean allDay,
			String description) {
		this();
		mBeginTime = beginTime;
		mEndTime = endTime;
		mAllDay = allDay;
		mDescription = description;
		mMatcher = mPattern.matcher(description);
		setRingerModes();
	}

	public CalendarEvent(long beginTime, long endTime, boolean allDay,
			String description) {
		this(new Date(beginTime), new Date(endTime), allDay, description);
	}

	public boolean matchesBeginTime(long millis) {
		Date target = new Date(millis);

		if ((target.getMonth() == mBeginTime.getMonth()
				&& target.getDate() == mBeginTime.getDate()
				&& target.getYear() == mBeginTime.getYear() && target
				.getHours() == mBeginTime.getHours())
				&& (target.getMinutes() == mBeginTime.getMinutes())) {
			return true;
		}
		return false;
	}

	public boolean matchesEndTime(long millis) {
		Date target = new Date(millis);

		if ((target.getMonth() == mEndTime.getMonth()
				&& target.getDate() == mEndTime.getDate()
				&& target.getYear() == mEndTime.getYear() && target.getHours() == mEndTime
				.getHours())
				&& (target.getMinutes() == mEndTime.getMinutes())) {
			return true;
		}
		return false;
	}

	public boolean endTimeHasPassed(long millis) {
		Date target = new Date(millis);
		if ((target.getMonth() > mEndTime.getMonth()
				|| target.getDate() > mEndTime.getDate()
				|| target.getYear() > mEndTime.getYear() || target.getHours() > mEndTime
				.getHours())
				|| (target.getMinutes() > mEndTime.getMinutes())) {
			return true;
		}
		return false;
	}

	public boolean changesRingMode() {
		if (mNbrMatchesFound == 0) {
			mMatchFound = mMatcher.find();
			mNbrMatchesFound += 1;
		}
		return mMatchFound;
	}

	private void setRingerModes() throws NullPointerException {
		final int begin = 0;
		final int end = 1;
		if (!changesRingMode())
			throw new NullPointerException("Ringer mode match was not found.");
		String[] modes = mMatcher.group(0).split(",");
		modes[begin] = modes[begin].substring(1);
		modes[end] = modes[end].substring(0, 1);
		if (modes[begin].length() == 1 && modes[end].length() == 1) {
			String[] ringModes = Utilities.RINGER_MODES_TEXT;
			String rmLetter = "";
			for (int i = 0; i < ringModes.length; i++) {
				rmLetter = ringModes[i].substring(0, 1).toLowerCase();
				if (modes[begin].equals(rmLetter))
					mBeginRingMode = i;
				if (modes[end].equals(rmLetter))
					mEndRingMode = i;
			}
		}

	}
}
