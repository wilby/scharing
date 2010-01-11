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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ScheduleDays extends Activity {
    
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {    	    	
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.schedules_days); 
        
    }
	
    
	
	/**
	 * A blanket listener for all the days of the week buttons to 
	 * view that days schedule.
	 * @param v The button view
	 */
    public void btn_Click(View v) {
    	Button b = (Button)v;
    	String[] days = Utilities.DAYS_OF_WEEK_TEXT;
    	String day = b.getText().toString();
    	String arrayDay;
    	for (int j = 0; j < days.length; j++) {
    		arrayDay = days[j];
    		if (arrayDay.equalsIgnoreCase(day)) {
    			DailyScheduleListActivity.WEEK_DAY = j;
    			break;
    		}
    	}
    	
    	Intent i = new Intent(this, DailyScheduleListActivity.class);
    	startActivity(i);    	
    }
    
    
    
}
