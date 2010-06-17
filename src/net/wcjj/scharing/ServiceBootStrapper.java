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
import android.util.Log;

/**
 *ServiceBootStrapper listens for the BOOT_COMPLETED broadcast and starts
 * net.wcjj.scharing.Service when the android boot process is completed.
 */
public class ServiceBootStrapper extends BroadcastReceiver {

	static final String TAG = "Scharing_ServiceBootStrapper";

	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			Intent servIntent = new Intent(context,
					net.wcjj.scharing.Service.class);
			context.startService(servIntent);
			// let the user know we are running in the background
			Log.i(TAG, "Scharing service starting");
			Utilities.scharingNotification(context, context
					.getString(R.string.bootstrap_message));
		} else {
			Log.e(TAG, "Received unexpected intent at boot completed "
					+ intent.toString());
		}
	}
}
