package net.ashishb.android_time_tracker;

import android.Manifest.permission;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
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
	private ArrayAdapter<String> listAdapter;
	private ListView listView;
	// TODO: add a UI Settings element for that.
	private boolean excludeLauncherPackage = true;
	private PackageManager pm = null;


	/** Called when the activity is first created. */
	@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			Context context = getApplicationContext();
			context.startService(new Intent(context, TimeTrackingService.class));
			pm  = getPackageManager();
		}

	@Override
		public void onResume() {
			super.onResume();
			String launcherPackageName = this.getLauncherPackageName();
			listView = getListView();
			listView.setTextFilterEnabled(true);
			Set< Entry<String, Integer> > values = 
				TimeTrackingService.getPackageUseCount().entrySet();
			if (values.size() == 0) {
				// This is really crude. I need to improve on this.
				finish();
				// SystemClock.sleep(1000);
				// values = TimeTrackingService.getPackageUseCount().entrySet();
			}
			TreeSet< Entry<String, Integer> > sortedSet = new TreeSet(
					new Comparator<Entry<String, Integer>>() {
				@Override
				public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
					return (-1) * (e1.getValue() - e2.getValue());
				}
			});
			sortedSet.addAll(values);
			Vector<String> entries = new Vector<String>();
			long sum = 0;
			Iterator<Entry<String, Integer>> iterator = sortedSet.iterator();
			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				if (!entry.getKey().equals(launcherPackageName)) {
					sum += entry.getValue();
				}
			}

			Iterator<Entry<String, Integer>> iterator2 = sortedSet.iterator();
			while (iterator2.hasNext()) {
				Entry<String, Integer> entry = iterator2.next();
				if (!entry.getKey().equals(launcherPackageName)) {
					long value = (100*entry.getValue())/sum;
					if (value > 0) {
						try {
							String label = pm.getApplicationInfo(
									entry.getKey(),
									PackageManager.GET_META_DATA).loadLabel(pm).toString();
							Drawable logo = pm.getApplicationLogo(entry.getKey());
							entries.add(label + ": " + (100*entry.getValue()/sum) + "%");
						} catch (PackageManager.NameNotFoundException e) {
							Log.e(TAG, "Trying to read label of non existant package " +
								 	entry.getKey());
						}
					}
				}
			}
			listAdapter = new ArrayAdapter(this,
					android.R.layout.simple_list_item_1, entries.toArray());
			listView.setAdapter(listAdapter);
		}

	public String getLauncherPackageName() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = getPackageManager().resolveActivity(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		return resolveInfo.activityInfo.packageName;
	}

}
