package net.wcjj.scharing;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;


public class SundayListActivity extends ListActivity {
	
	@Override
    public void onCreate(Bundle bundle) {
       super.onCreate(bundle);
       setContentView(R.layout.lv_container);    
       
       
       SimpleAdapter saSunday = new SimpleAdapter( 
				this, 
				Service.mSchedule.toSimpleAdapterMap(Time.SUNDAY),
				R.layout.main_item_two_line_row,
				new String[] {Schedule.SCHEDULE_DOW ,Schedule.SCHEDULED_TIME, Schedule.RINGER_MODE},
				new int[] { R.id.text0, R.id.text1, R.id.text2 }  );
       
       setListAdapter(saSunday);
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      boolean result = super.onCreateOptionsMenu(menu);
      menu.add(0, ADD_ITEM_ID, R.string.add_item );
      return result;
    }

    @Override
    public boolean onOptionsItemSelected(Menu.Item item) {
        switch ( item.getId() ) {
          case ADD_ITEM_ID:
				addItem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/


}
