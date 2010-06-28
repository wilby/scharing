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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.widget.TimePicker;

public class Utilities {		
	
	// The next three constants are keys in Scharing's SharedPreferences
	public static final String PREFERENCES_FILENAME = "ScharingPreferences";
	
	public static final String PREFERENCES_FIELD_SHOW_ALERTS = "showAlerts";
	
	public static final String PREFERENCES_FIELD_12_HOUR_CLOCK = "clock";	
	
	/*
	 * Ringer modes are actually integer values, the indexes of the strings 
	 * in this array match the int values in AudioManager so they can be used
	 * as output to the user.
	 */
	public static final String[] RINGER_MODES_TEXT = { "Silent", "Vibrate",
			"Ring" };
	
	// Sunday - Saturday = 0-6, The following constants match the
	// positions these selections occur in Scheduler.java's main.xml dropdown
	// for days.
	public static final String[] DAYS_OF_WEEK_TEXT = { "Sunday", "Monday",
			"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
			"Weekdays", "Weekend" };

	/**
	 * This a method for activities and the service to both use to for a 
	 * standard notification message. 
	 * 
	 * @param context Android application context
	 * @param message The message to display to the user in the notification.
	 */
	public static void scharingNotification(Context context, String message) {
		final String 
		RINGER_MODE_CHANGE_NOTIFY_TAG =	"RINGER MODE CHANGED";
		final int RINGER_MODE_CHANGE_NOTIFY_ID = 1;
		
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification n = new Notification(R.drawable.notification_icon,
				"Scharing Notification", System.currentTimeMillis());

		CharSequence contentTitle = "Scharing Info";
		CharSequence contentText = message;
		Intent notificationIntent = new Intent(context,
				net.wcjj.scharing.Service.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		n.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		nm.notify(RINGER_MODE_CHANGE_NOTIFY_TAG, RINGER_MODE_CHANGE_NOTIFY_ID,
				n);
	}
	
	/**
	 * This is a method that should be used across the entire porject to 
	 * format times so they can be sorted by a TreeMap. If all the Time objects
	 * have the same date and seconds then when converted to millis they will 
	 * be in a natural, easy to sort format.
	 * 
	 * @param hour The hour value of the time object to be returned
	 * @param min The minutes value of the time object to be returned
	 * @return Time formatted to epoch with custom hour and min.
	 */	
	public static Time normalizeToScharingTime(int hour, int min) {
		Time t = new Time();
		//epoch day, custom hour and minutes
		//set time to sec, min, hour, day, month, year
		t.set(00, min, hour, 1, 1, 1970);
		return t;
	}


}
