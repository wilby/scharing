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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class WeekViewDeleteUI extends Activity {
    
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {    	    	
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.week_view_delete); 
        
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
    
    
    
    public void btnBatchDelete_Click(View v) {
    	Intent i = new Intent(this, BatchDeleteUI.class);
    	startActivity(i); 
    }
    
    
    public void btnDeleteEntireSchedule_Click(View v) {    
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(getString(R.string.delete_entire_schedule))
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                Service.getRingSchedule().delEntireSchedule();
    	           }
    	       })
    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	AlertDialog alert = builder.create(); 
    	alert.show();
    }
    
    
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.week_view_delete_menu, menu);
		
		return true;
	}
    
    
    
    /**
     * This event is run when a menu item is clicked on the options (icon) menu
     * 
     * The options menu can not display a checkbox so we must change out the displayed
     * icon to give the effect of checked/unchecked.
     * 
     * Options are saved in a Properties file to disk when they are changed.
     */
    public boolean onOptionsItemSelected(MenuItem item) {    
    	final int HELP_ID = R.id.btnWeekViewDeleteHelp;
        switch (item.getItemId()) {
        case HELP_ID:        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(R.string.week_view_delete_helpdialog_title)
        			.setMessage(getString(R.string.week_view_delete_helpdialog_message))
        	       .setCancelable(false)
        	       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create(); 
        	alert.show();      	
            return true;        
        }
        return false;
    }
    
    
    
}
