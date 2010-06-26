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

import java.io.IOException;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DailyScheduleListActivity extends ListActivity {

	public static int WEEK_DAY;
	private DailyScheduleAdapter mAdapter;
	private final String TAG = "Scharing_DailyScheduleListActivity";

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.lv_container);

		this.setTitle(Utilities.DAYS_OF_WEEK_TEXT[WEEK_DAY]);
		Schedule schedule = Service.getRingSchedule();
		mAdapter = new DailyScheduleAdapter(this, schedule.getDay(WEEK_DAY),
				true /* switch to preferences choice*/);
		setListAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.daily_schedule_lv_menu, menu);
		return true;
	}

	public void mBtnDelete_Click() {
		Schedule ringSchedule = Service.getRingSchedule();

		LinearLayout row = null;		
		CheckBox cb = null;
		ListView lv = getListView();
		int rowCount = lv.getCount();				
		Long itemsTime;
		// iterate over the views in the ListView
		// and remove items from the schedule that
		// have been checked by the user
		// this also removes the selected times from Services
		// schedule object
		for (int i = 0; i < rowCount; i++) {
			row = (LinearLayout) lv.getChildAt(i);
			if (row != null) {						
				cb = (CheckBox) row.findViewById(R.id.CheckBox02);
				if (cb.isChecked()) {
					itemsTime = Long.parseLong(((TextView)	row
							.findViewById(R.id.txtId)).getText().toString());				
					
					ringSchedule.delRingSchedule(WEEK_DAY, 
							itemsTime);
					mAdapter.notifyDataSetChanged();			
				}
			}
		}

		// Save the changes to disk
		try {
			ringSchedule.saveSchedule(this);
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mBtnDelete:
			mBtnDelete_Click();
			return true;
		}
		return false;
	}

}
