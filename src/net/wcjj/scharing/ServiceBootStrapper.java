package net.wcjj.scharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class ServiceBootStrapper extends BroadcastReceiver {
	
    static final String TAG = "Scharing_ServiceBootStrapper";

    @Override
    public void onReceive(Context context, Intent intent) {
        
    	if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
    		Intent servIntent = new Intent(context, net.wcjj.scharing.Service.class);
        	context.startService(servIntent);         	
        	//let the user know we are running in the background
        	Utilities.scharingNotification(context, context.getString(R.string.bootstrap_message));		    
		} else {
		   Log.e(TAG, "Received unexpected intent " + intent.toString());   
		}
    	
    	Log.d(TAG, "Boot time recieve intent");      

        Log.i(TAG, "Scharing service starting");
        
    }
     
}
