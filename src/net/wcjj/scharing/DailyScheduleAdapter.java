package net.wcjj.scharing;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;
import java.util.TreeMap;

public class DailyScheduleAdapter extends BaseAdapter {

	private Boolean m12HourClock;
	private TreeMap<Long, Integer> mDaysSchedule;	
	private LayoutInflater mInflator;
	/**
	 * When using checkboxes in a listview you need to save the state because
	 * convertView will recycle the views and you want to place the checkbox 
	 * state to  what it was originally or you will have all sorts of unexpected 
	 * behavior.
	 */
	private HashMap<Long, Boolean> mCheckedItems;
	
	public DailyScheduleAdapter(Context context, 
			TreeMap<Long, Integer> dailySchedule, Boolean use12HourClock) {
		super();		
		mDaysSchedule = dailySchedule;		
		mInflator = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		//mDaysSchedule.remove(null);
		mCheckedItems = new HashMap<Long, Boolean>();
		m12HourClock = use12HourClock;				
	}
	
	public int getCount() {
		return mDaysSchedule.size();
	}

	public Object getItem(int position) {
		return mDaysSchedule.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
					
		View row = convertView;
		ViewHolder holder = null;
		
		if(row == null) {
			row = mInflator.inflate(R.layout.daily_schedule_list_row, null);			
			holder = new ViewHolder(row);
			row.setTag(holder);
		}
		else {
		  holder=(ViewHolder)row.getTag();
		  mCheckedItems.put(Long.parseLong(holder.getTxtId()
				  .getText().toString()), 
				  holder.getDeleteCheckbox().isChecked());
		}		
		
		Time t = new Time();
		Long key = Long.parseLong(
				mDaysSchedule.keySet().toArray()[position].toString());
		t.set(key);
			
		holder.getTxtId().setText(String.valueOf(key));
		holder.getTxtTime().setText(
				m12HourClock ? t.format("%I:%M %p") : t.format("%H:%M")
			);
		holder.getTxtRingMode().setText(
				Utilities.RINGER_MODES_TEXT[mDaysSchedule.get(key)]);		
		
		if (mCheckedItems.containsKey(key)) {
			holder.getDeleteCheckbox().setChecked(mCheckedItems.get(key));
		}
		else {
			holder.getDeleteCheckbox().setChecked(false);
		}
		
		return row;
		
	}

}

class ViewHolder extends Object {
	
	private View mParent;
	private TextView mTxtId;
	private TextView mTxtTime;
	private TextView mTxtRingMode;
	private CheckBox mDeleteCheckbox;
 	
	ViewHolder(View parentView) {
		mParent = parentView;
	}
	
	TextView getTxtId() {
		if(mTxtId == null)
			mTxtId = (TextView)mParent.findViewById(R.id.txtId);
		return mTxtId;
	}
	
	TextView getTxtTime() {
		if(mTxtTime == null)
			mTxtTime = (TextView)mParent.findViewById(R.id.txtTime);
		return mTxtTime;	
	}
	
	TextView getTxtRingMode() {
		if(mTxtRingMode == null)
			mTxtRingMode = (TextView)mParent.findViewById(R.id.txtRingMode);
		return mTxtRingMode;
	}
	
	CheckBox getDeleteCheckbox() {
		if(mDeleteCheckbox == null)
			mDeleteCheckbox = (CheckBox)mParent.findViewById(R.id.CheckBox02);
		return mDeleteCheckbox;
	}	
}

