package net.ashishb.android_time_tracker;

import android.Manifest.permission;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

public class Main extends ListActivity
{
		public static final String TAG = "AndroidTimeTracker";
		ArrayAdapter<String> listAdapter;
		ListView listView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			Context context = getApplicationContext();
			context.startService(new Intent(context, TimeTrackingService.class));
		}

		@Override
		public void onResume() {
			super.onResume();
			listView = getListView();
			listView.setTextFilterEnabled(true);
			HashMap<String, Integer> values = 
				TimeTrackingService.getPackageUseCount();
			Vector<String> entries = new Vector<String>();
			for (Entry<String, Integer> entry : values.entrySet()) {
				entries.add(entry.getKey() + ": " + entry.getValue());
			}
			listAdapter = new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, entries.toArray());
			listView.setAdapter(listAdapter);
    }
}
