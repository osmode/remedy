package com.remedy.glass;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class AppLifecycleHandler implements ActivityLifecycleCallbacks {
	// I use two separate variables here. You can, of course, just use one and
	// increment/decrement it instead of using two and incrementing both.
	private static int resumed;
	private static int stopped;
	
	
	
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		android.util.Log.i("LifecycleMontor", "Activity " + activity.getLocalClassName() + " created, r count is " + resumed + " scount is " + stopped);
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
	}

	@Override
	public void onActivityResumed(Activity activity) {
		android.util.Log.i("LifecycleMontor", "Activity " + activity.getLocalClassName() + " resumed, r count is " + resumed + " scount is " + stopped);
		++resumed;
	}

	@Override
	public void onActivityPaused(Activity activity) {
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
	}

	@Override
	public void onActivityStarted(Activity activity) {
	}

	@Override
	public void onActivityStopped(Activity activity) {
		++stopped;
		android.util.Log.i("LifecycleMontor", "Activity " + activity.getLocalClassName() + " stopped, r count is " + resumed + " scount is " + stopped);
		android.util.Log.w("test", "application is being backgrounded: " + (resumed == stopped));
	}

	public static boolean isApplicationInForeground() {
		return resumed > stopped;
	}

	// Alternate fuction, for convenience
	public static boolean isApplicationBeingBackgrounded() {
		return resumed == stopped;
	}
}
