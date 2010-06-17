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

	private static final int RINGER_MODE_CHANGE_NOTIFY_ID = 1;
	private static final String RINGER_MODE_CHANGE_NOTIFY_TAG = "RINGER MODE CHANGED";

	// Sunday - Saturday = 0-6, The following constants match the
	// positions these selections occur in Scheduler.java's main.xml dropdown
	// for days.
	public static final String PREFERENCES_FILENAME = "ScharingPreferences";
	public static final String PREFERENCES_FIELD_SHOW_ALERTS = "showAlerts";
	public static final int WEEKDAYS = 7;
	public static final int WEEKENDS = 8;
	public static final String[] RINGER_MODES_TEXT = { "Silent", "Vibrate",
			"Ring" };
	public static final String[] DAYS_OF_WEEK_TEXT = { "Sunday", "Monday",
			"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
			"Weekdays", "Weekend" };

	public static void scharingNotification(Context context, String message) {
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

	public static String toScheduleTimeFormat(long systemTimeInMillis) {
		Time t = new Time();
		t.set(systemTimeInMillis);
		String minutes = String.valueOf(t.minute);
		// return time string in format [H]H:mm, military time 0-23
		return String.valueOf(t.hour) + ":"
				+ (minutes.length() == 1 ? "0" + minutes : minutes);
	}

	public static String toScheduleTimeFormat(TimePicker tp) {
		String t = String.valueOf(tp.getCurrentHour())
				+ ":"
				+ (String.valueOf(tp.getCurrentMinute()).length() == 1 ? "0"
						+ tp.getCurrentMinute().toString() : tp
						.getCurrentMinute().toString());

		return t;
	}

}
