/**
 * 	  Scharing - Allows you to set a ring, vibrate and silence shedule for your 
 * 	  android device.
 *   
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class reads from the Schedule (set by the user) and changes the ring
 * mode based on the users preferences at a given time of day. This class needs
 * to run constantly in the background. It is started on device boot or when the
 * UI is started.
 **/
public class Service extends android.app.Service {

	private AudioManager mAudioManager;
	private ArrayList<CalendarEvent> mActiveCalEvents;
	private final String TAG = "Scharing_Service";

	private static boolean mShowAlerts;
	private static Schedule mRingSchedule;

	public static Schedule getRingSchedule() {
		return mRingSchedule;
	}

	@Override
	public void onCreate() {
		registerReceiver(new TimeTickListener(), new IntentFilter(
				Intent.ACTION_TIME_TICK));
		registerReceiver(new ScharingSetAlertsListener(), 
				new IntentFilter(ScharingIntents.HIDE_SHARING_ALERTS));
		registerReceiver(new ScharingSetAlertsListener(), 
				new IntentFilter(ScharingIntents.SHOW_SHARING_ALERTS));
		registerReceiver(new ScharingSetAlertsListener(), 
				new IntentFilter(ScharingIntents.REQUEST_SHOW_ALERTS_STATE));

		mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		mActiveCalEvents = new ArrayList<CalendarEvent>();

		try {
			boolean fileNameMatches = false;
			String schedulesFileName = "schedule.obj";
			String[] fileNames = fileList();
			if (fileNames.length > 0)
				for (int i = 0; i < fileNames.length; i++) {
					fileNameMatches = fileNames[i] == schedulesFileName;
					if (fileNameMatches)
						break;
				}

			if (fileNameMatches) {
				mRingSchedule = Schedule.loadSchedule(this);
			} else {
				// First run create and persists schedule
				mRingSchedule = new Schedule();
				mRingSchedule.saveSchedule(this);
			}
			loadPreferences();
		} catch (IOException e) {
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
	public void onDestroy() {
		mRingSchedule = null;
		mAudioManager = null;
		mActiveCalEvents = null;
		super.onDestroy();
	}

	public void stopService() {
		// Save our schedule if the services is killed
		saveSchedule();
		savePreferences();
		mRingSchedule = null;
	}

	@Override
	public IBinder onBind(Intent arg0) {		
		return null;
	}

	/**
	 * Save the schedule and let the user know that their next ringer mode
	 * change will not occur.
	 */
	private void saveSchedule() {
		try {
			mRingSchedule.saveSchedule(this);
			Utilities.scharingNotification(getApplicationContext(),
					getString(R.string.service_shutdown_warning));
		} catch (IOException e) {			
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 *Respond to system time advancements, occurs every one minute. If there is
	 * a matching day/time in the schedule then change the ringer mode to the
	 * mode in the schedule.
	 **/
	public class TimeTickListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			long millis = System.currentTimeMillis();

			if (!setModeByCalEventEnd(millis)) {
				if (!setModeByCalEventBegin(millis)) {					
					Time t = new Time();
					t.set(millis);
					int weekday = t.weekDay;
					if (mRingSchedule.hasTime(weekday, millis)) {
						mAudioManager.setRingerMode(mRingSchedule
								.getRingerMode(weekday, millis));
						showRingChangeAlert();
					}
				}
			}
		}
	}

	private boolean setModeByCalEventBegin(long millis) {
		ArrayList<CalendarEvent> evts;
		// Make sure that a calendar is present on the system before continuing.
		try {
			evts = getTodaysCalEvents(millis);
		} catch (NullContentProviderException ncpe) {
			return false;
		}

		int nbrEvts = evts.size();
		// If there are no events we will return to set mode by schedule
		if (nbrEvts == 0 || evts == null)
			return false;

		for (int i = 0; i < nbrEvts; i++) {
			CalendarEvent ce = evts.get(i);
			if (ce.changesRingMode()) {
				if (ce.matchesBeginTime(millis)) {
					mActiveCalEvents.add(ce);
					mAudioManager.setRingerMode(ce.getBeginRingMode());
					showRingChangeAlert();
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * Cycle through the currently active events and determine if one of 
	 * the end times matches the millis parameter.
	 * 
	 * @param millis
	 *            The date in milliseconds to be compared to the ending event
	 *            time.
	 * @return boolean If a calendar event end time matches the input date or
	 *         not
	 */
	private boolean setModeByCalEventEnd(long millis) {
		ArrayList<CalendarEvent> evts = mActiveCalEvents;
		int nbrEvts = evts.size();
		// If there are no events we will return to set mode by schedule
		if (nbrEvts == 0 || evts == null)
			return false;

		for (int i = 0; i < nbrEvts; i++) {
			CalendarEvent ce = evts.get(i);
			if (ce.changesRingMode()) {
				if (ce.matchesEndTime(millis)) {
					mAudioManager.setRingerMode(ce.getEndRingMode());
					showRingChangeAlert();
					evts.remove(i);
					return true;
				}
				/*
				 * If more than one calendar has an event at the same time it
				 * would be possible for one event to be bypassed by the match
				 * and never be removed from the collection. This should get rid
				 * of any stragglers.
				 */
				else if (ce.endTimeHasPassed(millis)) {
					evts.remove(i);
				}
			}
			ce = null;
		}
		return false;
	}

	private ArrayList<CalendarEvent> getTodaysCalEvents(long millis)
			throws NullContentProviderException {

		ContentResolver contentResolver = this.getContentResolver();
		final Cursor cursor = contentResolver.query(Uri
				.parse("content://calendar/calendars"),
				(new String[] { "_id" }), null, null, null);

		if (cursor == null)
			throw new NullContentProviderException(
			"This version of android does not have the calendar app present.");

		ArrayList<String> ids = new ArrayList<String>();

		while (cursor.moveToNext()) {
			ids.add(cursor.getString(0));
		}

		Uri.Builder builder = Uri.parse("content://calendar/instances/when")
				.buildUpon();

		ContentUris.appendId(builder, millis);
		ContentUris.appendId(builder, millis);

		Cursor eventCursor;

		ArrayList<CalendarEvent> calEvents = new ArrayList<CalendarEvent>();

		for (int i = 0; i < ids.size(); i++) {

			eventCursor = getContentResolver().query(builder.build(),
					new String[] { "description", "begin", "end", "allDay" },
					"Calendars._id=" + ids.get(i), null, "begin ASC");
			if (eventCursor.getCount() != 0) {
				while (eventCursor.moveToNext()) {
					calEvents.add(new CalendarEvent(new Date(eventCursor
							.getLong(1)), new Date(eventCursor.getLong(2)),
							!eventCursor.getString(3).equals("0"), eventCursor
									.getString(0)));
				}
				eventCursor = null;
			}
		}

		return calEvents;
	}

	private void showRingChangeAlert() {
		if (mShowAlerts)
			Utilities.scharingNotification(getApplicationContext(),
					getString(R.string.ring_mode_changed)
							+ " "
							+ Utilities.RINGER_MODES_TEXT[mAudioManager
									.getRingerMode()] + " @: ");

	}

	public class ScharingSetAlertsListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			mShowAlerts = intent.getAction().equals(
					ScharingIntents.SHOW_SHARING_ALERTS) ? true : false;
		}
	}

	/**
	 * Listen for request of the state of showing scharing alerts. If a
	 * broadcast is recieved respond with on of two intents letting the
	 * requester know true or false
	 */
	public class ShowAlertsStateListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mShowAlerts) {
				sendBroadcast(new Intent(ScharingIntents.SHOW_SHARING_ALERTS));
			} else {
				sendBroadcast(new Intent(ScharingIntents.HIDE_SHARING_ALERTS));
			}
		}
	}

	private void loadPreferences() {
		SharedPreferences scharingPreferences = getSharedPreferences(
				Utilities.PREFERENCES_FILENAME, MODE_PRIVATE);
		mShowAlerts = scharingPreferences.getBoolean(
				Utilities.PREFERENCES_FIELD_SHOW_ALERTS, true);
	}

	private void savePreferences() {
		SharedPreferences scharingPreferences = getSharedPreferences(
				Utilities.PREFERENCES_FILENAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = scharingPreferences.edit();
		editor.putBoolean(Utilities.PREFERENCES_FIELD_SHOW_ALERTS, mShowAlerts);
		editor.commit();
	}

}
