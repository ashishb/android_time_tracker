package net.ashishb.android_time_tracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TimeTrackingService extends IntentService {

	public static final String TAG = "AndroidTimeTracker";
  // Change it to 10 seconds for the release.
	public static final int SLEEP_TIME_IN_MILLISECONDS = 1000;
	// Dump to persistant store after this many recordings of pkg use count.
	public static final int PERSISTANT_DUMP_INTERVAL_IN_RECORDING_UNITS = 65535;
	// Map of Package name to count of seconds for which app has been used.
	private static HashMap<String, Integer> pkgUseCount =
	 	new HashMap<String, Integer>(100);

	SharedPreferences pkgCountPersistantStore = null;

	public TimeTrackingService() {
		super("TimeTrackingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "Service started.");
		// One time init of pkgUseCount.
		if (pkgCountPersistantStore == null) {
			Log.i(TAG, "Reading from the persistant store.");
			pkgCountPersistantStore = PreferenceManager.getDefaultSharedPreferences(this);
			Map<String, ?> pkgCountMap = pkgCountPersistantStore.getAll();
			for ( Entry<String, ?> entry: pkgCountMap.entrySet()) {
				String pkgName = entry.getKey();
				int count = (Integer)entry.getValue();
				pkgUseCount.put(pkgName, count);
				Log.d(TAG, String.format("pkgName: %s count: %d", pkgName, count));
			}
		}

		int dump_count = 0;
		while(true) {
			dump_count++;
			// TODO: also, check that the screen is ON.
			String value = getCurrentlyUsedPackage();
			Integer currentUse = pkgUseCount.get(value);
			if (currentUse == null) {
				pkgUseCount.put(value, 0 + 1);
			} else {
				pkgUseCount.put(value, currentUse.intValue() + 1);
			}
			Log.i(TAG, "" + value);
			SystemClock.sleep(SLEEP_TIME_IN_MILLISECONDS);

			if ( (dump_count & PERSISTANT_DUMP_INTERVAL_IN_RECORDING_UNITS) == 0) {
				dumpToPersistantStore();
			}
		}
	}

	public static HashMap<String, Integer> getPackageUseCount() {
		return pkgUseCount;
	}

	private void dumpToPersistantStore() {
		Log.d(TAG, "Dumping to persistant storage.");
		SharedPreferences.Editor editor = pkgCountPersistantStore.edit();
		if (pkgUseCount != null) {
			for (Entry<String, Integer> entry: pkgUseCount.entrySet()) {
				editor.putInt(entry.getKey(), entry.getValue());
			}
		}
		editor.commit();
	}

	private String getCurrentlyUsedPackage() {
		ActivityManager mActivityManager =
		 	(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> RunningTask =
		 	mActivityManager.getRunningTasks(1);
		ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
		return ar.topActivity.getPackageName().toString();
	}
}
