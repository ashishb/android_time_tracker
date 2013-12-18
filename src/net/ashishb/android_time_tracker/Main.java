package net.ashishb.android_time_tracker;

import android.Manifest.permission;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.TextView;

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
	// TODO: add a UI Settings element for this setting.
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
      String packageName = this.getApplicationContext().getPackageName();
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
			TreeSet< Entry<String, Integer>> sortedSet = new TreeSet(
					new Comparator<Entry<String, Integer>>() {
				@Override
				public int compare(
					Entry<String, Integer> e1, Entry<String, Integer> e2) {
					return (-1) * (e1.getValue() - e2.getValue());
				}
			});
			sortedSet.addAll(values);
			// Vector<String> entries = new Vector<String>();
			Vector<PackageInfoEntry> entries = new Vector<PackageInfoEntry>();
			long sum = 0;
			Iterator<Entry<String, Integer>> iterator = sortedSet.iterator();
			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				if (!entry.getKey().equals(launcherPackageName) &&
            !entry.getKey().equals(packageName)) {
					sum += entry.getValue();
				}
			}

			Iterator<Entry<String, Integer>> iterator2 = sortedSet.iterator();
			while (iterator2.hasNext()) {
				Entry<String, Integer> entry = iterator2.next();
        // Ignore Launcher app.
        // Ignore the time tracker app.
				if (!entry.getKey().equals(launcherPackageName) &&
            !entry.getKey().equals(packageName))  {
					double value = (double)entry.getValue()/sum;
					if (value > 0.01) {
						try {
							PackageInfoEntry tmp = new PackageInfoEntry();
							tmp.packageLabel = pm.getApplicationInfo(
									entry.getKey(),
									PackageManager.GET_META_DATA).loadLabel(pm).toString();
							tmp.packageName = entry.getKey();
							tmp.packageIcon = pm.getApplicationIcon(entry.getKey());
							tmp.usage = value;
							entries.add(tmp);
						} catch (PackageManager.NameNotFoundException e) {
							Log.e(TAG, "Trying to read label of non existant package " +
								 	entry.getKey());
						}
					}
				}
			}
			listAdapter = new PackageInfoArrayAdapter(this,	entries);
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

class PackageInfoEntry {
	public String packageName;  // Fully qualified name of the package.
	public String packageLabel; // Human readable label of the package.
	public Drawable packageIcon; // Icon of the package;
	public double usage;  // Expressed as a fraction of total usage.
};

class PackageInfoArrayAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final Vector<PackageInfoEntry> values;

  public PackageInfoArrayAdapter(Context context, Vector<PackageInfoEntry> values) {
		super(context, R.layout.main, new String[values.size()]);
		this.context = context;
		this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.main, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.label);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
		// Set the package label (and not fully qualified name) as text.
		textView.setText(values.get(position).packageLabel + "\t" + (int)(values.get(position).usage * 100) + "%");
  	String s = values.get(position).packageName;
		imageView.setImageDrawable(values.get(position).packageIcon);
		Log.d(Main.TAG, "Icon is " + values.get(position).packageIcon);
		return rowView;
  }
} 
