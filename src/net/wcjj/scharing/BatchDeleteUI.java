package net.wcjj.scharing;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.text.format.Time;

public class BatchDeleteUI extends Activity {
	
	
	
	final String TAG = "Scharing_BatchDeleteUI";
	private boolean mHighlightColor = false;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
        setContentView(R.layout.batch_delete); 
		setTitle(getString(R.string.batch_dialog_title));
        
        InitializeSpinner();    
        
	}
	
	
	
	public void InitializeSpinner() {
		Spinner spBatch = (Spinner)findViewById(R.id.spBatchDeleteWeekday);
		ArrayAdapter<CharSequence> adpBatchWeekday = ArrayAdapter.createFromResource(
	            this, R.array.batchWeekday, android.R.layout.simple_spinner_item);    
		spBatch.setAdapter(adpBatchWeekday);
		
		
	}
	
	
	
	public void btnBatchDelete_Click(View v) {	
		final int weekdays = 0;
		final int weekends = 1;			
		
		Spinner spWeekday = (Spinner)findViewById(R.id.spBatchDeleteWeekday);
    	TimePicker tpStartTime = (TimePicker) findViewById(R.id.tpBatchTime);
    	TextView tvMessages = (TextView)findViewById(R.id.tvBatchDeleteMessages);
    	
    	String time = Utilities.toScheduleTimeFormat(tpStartTime);
    	int selectedValue = spWeekday.getSelectedItemPosition();
    	Schedule ringSchedule = Service.getRingSchedule();
    	
    	
	    	if(selectedValue == weekdays) {
	    		for(int i = Time.MONDAY; i <= Time.FRIDAY; i++){
	    			if(ringSchedule.hasTime(i, time))
	    				ringSchedule.delRingSchedule(i, time);
	    		}
	    	}
	    	else if (selectedValue == weekends) {
    			if(ringSchedule.hasTime(Time.SUNDAY, time)) {
    				ringSchedule.delRingSchedule(Time.SUNDAY, time);
				if(ringSchedule.hasTime(Time.SATURDAY, time))
					ringSchedule.delRingSchedule(Time.SATURDAY, time);
	    		
	    	}
	    	else {
	    		
	    		for(int i = Time.SUNDAY; i <= Time.SATURDAY; i++){
	    			if(ringSchedule.hasTime(i, time))	
	    				ringSchedule.delRingSchedule(i, time);
	    		}
	    	}
	    }
	   
    	
    	try {
			ringSchedule.saveSchedule(getApplicationContext());
			tvMessages.setText(getString(R.string.delete_success));
			if(mHighlightColor) {
				tvMessages.setTextColor(Color.LTGRAY);
				mHighlightColor = false;
			}
			else {
				tvMessages.setTextColor(Color.parseColor(getString(R.color.font_color_for_dark_back)));
				mHighlightColor = true;
			
			}
		} catch (IOException e) {			
			Log.e(TAG, Log.getStackTraceString(e));
			Utilities.scharingNotification(getApplicationContext(), getString(R.string.io_error));
		}
	
	}
	
	
}
