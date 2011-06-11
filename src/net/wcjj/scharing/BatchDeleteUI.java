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
		Spinner spBatch = (Spinner) findViewById(R.id.spBatchDeleteWeekday);
		ArrayAdapter<CharSequence> adpBatchWeekday = ArrayAdapter
				.createFromResource(this, R.array.batchWeekday,
						android.R.layout.simple_spinner_item);
		spBatch.setAdapter(adpBatchWeekday);

	}

	public void btnBatchDelete_Click(View v) {
		final int weekdays = 0;
		final int weekends = 1;

		Spinner spWeekday = (Spinner) findViewById(R.id.spBatchDeleteWeekday);
		TimePicker tpStartTime = (TimePicker) findViewById(R.id.tpBatchTime);
		TextView tvMessages = (TextView) findViewById(R.id.tvBatchDeleteMessages);

		Time time =  new Time();
		time.set(00, tpStartTime.getCurrentMinute(), 
				tpStartTime.getCurrentHour(), 01, 01, 1970);
		long timeInMillis = time.toMillis(true);
		
		int selectedValue = spWeekday.getSelectedItemPosition();
		Schedule ringSchedule = Service.getRingSchedule();

		if (selectedValue == weekdays) {
			for (int i = Time.MONDAY; i <= Time.FRIDAY; i++) {
				if (ringSchedule.hasTime(i, timeInMillis))
					ringSchedule.delRingSchedule(i, timeInMillis);
			}
		} else if (selectedValue == weekends) {
			if (ringSchedule.hasTime(Time.SUNDAY, timeInMillis)) {
				ringSchedule.delRingSchedule(Time.SUNDAY, timeInMillis);
				if (ringSchedule.hasTime(Time.SATURDAY, timeInMillis))
					ringSchedule.delRingSchedule(Time.SATURDAY, timeInMillis);

			} else {

				for (int i = Time.SUNDAY; i <= Time.SATURDAY; i++) {
					if (ringSchedule.hasTime(i, timeInMillis))
						ringSchedule.delRingSchedule(i, timeInMillis);
				}
			}
		}

		try {
			ringSchedule.saveSchedule(getApplicationContext());
			tvMessages.setText(getString(R.string.delete_success));
			if (mHighlightColor) {
				tvMessages.setTextColor(Color.LTGRAY);
				mHighlightColor = false;
			} else {
				tvMessages
						.setTextColor(Color
								.parseColor(getString(R.color.font_color_for_dark_back)));
				mHighlightColor = true;

			}
		} catch (IOException e) {
			Log.e(TAG, Log.getStackTraceString(e));
			Utilities.scharingNotification(getApplicationContext(),
					getString(R.string.io_error));
		}

	}

}
