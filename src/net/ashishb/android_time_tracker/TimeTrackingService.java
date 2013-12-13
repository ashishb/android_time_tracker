package net.ashishb.android_time_tracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class TimeTrackingService extends IntentService {

	public static final String TAG = "AndroidTimeTracker";
  // Change it to 10 seconds for the release.
	public static final int SLEEP_TIME_IN_MILLISECONDS = 1000;
	// Map of Package name to count of seconds for which app has been used.
	private static HashMap<String, Integer> pkgUseCount = new HashMap<String, Integer>(100);

	public TimeTrackingService() {
		super("TimeTrackingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "Service started.");
		while(true) {
			// TODO: also, check that the screen is ON.
			String value = getCurrentlyUsedPackage();
			Integer currentUse = pkgUseCount.get(value);
			if (currentUse == null) {
				pkgUseCount.put(value, 0 + 1);
			} else {
				pkgUseCount.put(value, currentUse.intValue() + 1);
			}
			Log.i(TAG, "" + value);
			SystemClock.sleep(1000);
		}
	}

	public static HashMap<String, Integer> getPackageUseCount() {
		return pkgUseCount;
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
