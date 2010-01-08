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
