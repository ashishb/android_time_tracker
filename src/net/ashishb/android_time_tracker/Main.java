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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
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
			Set< Entry<String, Integer> > values = 
				TimeTrackingService.getPackageUseCount().entrySet();
			TreeSet< Entry<String, Integer> > sortedSet = new TreeSet(new Comparator<Entry<String, Integer>>() {
				@Override
				public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
					return (-1) * (e1.getValue() - e2.getValue());
				}
			});
			sortedSet.addAll(values);
			Vector<String> entries = new Vector<String>();
			Iterator<Entry<String, Integer>> iterator = sortedSet.iterator();
			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				entries.add(entry.getKey() + ": " + entry.getValue());
			}
			listAdapter = new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, entries.toArray());
			listView.setAdapter(listAdapter);
		}
}
